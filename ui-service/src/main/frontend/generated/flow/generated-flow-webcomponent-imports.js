import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '0430dfa78a905da6fb6f10a292b0830a0af4e2923afbc18fa82aae6f7b683403') {
    pending.push(import('./chunks/chunk-e045ef5a031e111b82410052df809428f03e3f7ba390a8b9f35fceb5b00478e4.js'));
  }
  if (key === '0361c7bdaa63285d8a4a330c15827ddaabb31c1dc31c4f0e40235eee91be2ef3') {
    pending.push(import('./chunks/chunk-e045ef5a031e111b82410052df809428f03e3f7ba390a8b9f35fceb5b00478e4.js'));
  }
  if (key === '385dd6ae902a59f7db7459618f93f6d81cdf61ab6c1c190eaf2955f65c648b2a') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '52ba0ab5e9c6f88fb1f298024e81683b11c05fd7c355afd877f4f3d3fd2910b6') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '5842162c740c88eb96a3034249f540b8e3afb84091c1add18bb10a31e09ff862') {
    pending.push(import('./chunks/chunk-69d24a4085d6032ca3392a868036ebc716b76ba2542382d00c8f9e30b1f8bf63.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}