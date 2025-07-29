import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '0430dfa78a905da6fb6f10a292b0830a0af4e2923afbc18fa82aae6f7b683403') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === 'f5f2ffa1f5e9e1c13f3d1601fe36624a8b370ef6e43335162e2a9fab1af882d3') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '806c39bd3f5af134695ef75e141cf85bae2ea4a55fe48a5c8a93d2d058893899') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '52ba0ab5e9c6f88fb1f298024e81683b11c05fd7c355afd877f4f3d3fd2910b6') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '385dd6ae902a59f7db7459618f93f6d81cdf61ab6c1c190eaf2955f65c648b2a') {
    pending.push(import('./chunks/chunk-cd6a3b6f6faebed1493d078a30ed7b10109a53d1122ed5addaf07959b749bfe6.js'));
  }
  if (key === '0361c7bdaa63285d8a4a330c15827ddaabb31c1dc31c4f0e40235eee91be2ef3') {
    pending.push(import('./chunks/chunk-58c3aaae745008d7b2bb140d13a5da5b3cf12557d77427c743e9ea7c92cbe7de.js'));
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