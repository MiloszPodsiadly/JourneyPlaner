package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.UserProfileDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@PageTitle("Your Profile")
@Route("profile")
@PermitAll
public class UserProfileView extends VerticalLayout {

    private final RestTemplate rest = new RestTemplate();

    // Docker service URL kept as string
    private static final String PROFILE_API_BASE = "http://user-service:8081/api/user-profiles";

    private static final List<String> AVATARS = List.of(
            "/images/avatars/fox.png",
            "/images/avatars/bear.png",
            "/images/avatars/owl.png",
            "/images/avatars/wolf.png",
            "/images/avatars/panda.png",
            "/images/avatars/tiger.png"
    );
    private static final String DEFAULT_AVATAR = "/images/avatars/owl.png";

    private Long profileId;
    private Long userId;

    private final Image currentAvatar = new Image();
    private String selectedAvatarUrl = DEFAULT_AVATAR;

    private final TextField displayName = new TextField("Display name");
    private final TextArea bio = new TextArea("Bio");

    private final Button save = new Button("💾 Save");
    private final Button back = new Button("⬅️ Back to menu", e -> UI.getCurrent().navigate("main-menu"));

    public UserProfileView() {
        setWidth("100%");
        setMaxWidth("1000px");
        setPadding(true);
        setSpacing(true);

        add(new H1("⚙️ Your Profile"));
        add(buildHeaderRow());
        add(buildMainForm());
        add(buildAvatarPicker());
        add(buildActions());

        loadProfile();
    }

    private HorizontalLayout buildHeaderRow() {
        currentAvatar.setWidth("96px");
        currentAvatar.setHeight("96px");
        currentAvatar.getStyle().setBorderRadius("50%");
        currentAvatar.setSrc(DEFAULT_AVATAR);

        displayName.setWidth("260px");
        displayName.getStyle().set("margin-left", "12px");
        displayName.setRequiredIndicatorVisible(true);
        displayName.setHelperText("Required");

        HorizontalLayout row = new HorizontalLayout(currentAvatar, displayName);
        row.setAlignItems(Alignment.CENTER);
        row.setWidthFull();
        return row;
    }

    private FormLayout buildMainForm() {
        bio.setHeight("110px");
        bio.setWidth("min(700px,100%)");

        FormLayout form = new FormLayout();
        form.add(bio);
        form.setColspan(bio, 2);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("640px", 2)
        );
        return form;
    }

    private VerticalLayout buildAvatarPicker() {
        var title = new Span("Choose an avatar");
        title.getStyle().set("font-size", "1.1rem").set("font-weight", "600");

        FlexLayout grid = new FlexLayout();
        grid.getStyle().set("gap", "12px");
        grid.setWidthFull();

        AVATARS.forEach(url -> {
            Image img = new Image(url, "avatar");
            img.setWidth("72px");
            img.setHeight("72px");
            img.getStyle()
                    .setBorderRadius("50%")
                    .setCursor("pointer")
                    .set("box-shadow", "0 0 0 2px transparent")
                    .set("transition", "box-shadow 120ms ease");

            img.addClickListener(e -> {
                selectedAvatarUrl = url;
                highlightSelection(grid, url);
                currentAvatar.setSrc(url);
            });

            grid.add(img);
        });

        highlightSelection(grid, selectedAvatarUrl);

        var wrap = new VerticalLayout(title, grid);
        wrap.setPadding(false);
        wrap.setSpacing(false);
        wrap.setWidthFull();
        return wrap;
    }

    private void highlightSelection(FlexLayout grid, String pickedUrl) {
        grid.getChildren().forEach(c -> {
            if (c instanceof Image i) {
                boolean picked = pickedUrl.equals(i.getSrc());
                i.getStyle().set("box-shadow", picked
                        ? "0 0 0 3px var(--lumo-primary-color)"
                        : "0 0 0 2px transparent");
            }
        });
    }

    private HorizontalLayout buildActions() {
        save.addClickListener(e -> saveProfile());
        HorizontalLayout actions = new HorizontalLayout(save, back);
        actions.setSpacing(true);
        return actions;
    }

    private void loadProfile() {
        try {
            String spotifyId = getSpotifyIdFromCookie();
            if (spotifyId == null) {
                Notification.show("❌ No Spotify ID found in cookie");
                currentAvatar.setSrc(DEFAULT_AVATAR);
                selectedAvatarUrl = DEFAULT_AVATAR;
                return;
            }

            String url = PROFILE_API_BASE + "/" + UriUtils.encodePathSegment(spotifyId, StandardCharsets.UTF_8);
            ResponseEntity<UserProfileDto> resp = rest.getForEntity(url, UserProfileDto.class);

            UserProfileDto dto = resp.getBody();
            if (!resp.getStatusCode().is2xxSuccessful() || dto == null) {
                log.error("Profile GET failed: status={} body=null?", resp.getStatusCode());
                currentAvatar.setSrc(DEFAULT_AVATAR);
                selectedAvatarUrl = DEFAULT_AVATAR;
                Notification.show("❌ Failed to load profile");
                return;
            }

            this.profileId = dto.id();
            this.userId = dto.userId();

            displayName.setValue(dto.displayName() != null ? dto.displayName() : "");
            bio.setValue(dto.bio() != null ? dto.bio() : "");

            String avatarUrl = (dto.avatarUrl() != null && !dto.avatarUrl().isBlank())
                    ? dto.avatarUrl()
                    : DEFAULT_AVATAR;

            currentAvatar.setSrc(avatarUrl);
            selectedAvatarUrl = avatarUrl;

        } catch (Exception e) {
            currentAvatar.setSrc(DEFAULT_AVATAR);
            selectedAvatarUrl = DEFAULT_AVATAR;
            log.error("Error loading profile", e);
            Notification.show("❌ Error loading profile");
        }
    }

    private void saveProfile() {
        try {
            String spotifyId = getSpotifyIdFromCookie();
            if (spotifyId == null) {
                Notification.show("❌ No Spotify ID found in cookie");
                return;
            }

            String nameVal = displayName.getValue() == null ? "" : displayName.getValue().trim();
            if (nameVal.isBlank()) {
                Notification.show("⚠️ Display name cannot be empty");
                displayName.focus();
                return;
            }

            String avatarToSend = selectedAvatarUrl;
            if (!avatarToSend.startsWith("http")) {
                HttpServletRequest req = (HttpServletRequest) VaadinService.getCurrentRequest();
                String base = req.getScheme() + "://" + req.getServerName()
                        + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
                avatarToSend = base + avatarToSend;
            }

            UserProfileDto dto = new UserProfileDto(
                    profileId,
                    userId,
                    nameVal,
                    bio.getValue(),
                    avatarToSend
            );

            String url = PROFILE_API_BASE + "/" + UriUtils.encodePathSegment(spotifyId, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserProfileDto> entity = new HttpEntity<>(dto, headers);

            rest.exchange(url, HttpMethod.PUT, entity, UserProfileDto.class);

            Notification.show("✅ Profile saved");
        } catch (Exception e) {
            log.error("Error saving profile", e);
            Notification.show("❌ Failed to save profile");
        }
    }

    private String getSpotifyIdFromCookie() {
        var req = VaadinService.getCurrentRequest();
        if (req == null) return null;
        var cookies = req.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if ("spotify_id".equals(cookie.getName()) || "spotifyId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}