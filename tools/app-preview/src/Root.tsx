import React from "react";
import { Composition, Still } from "remotion";
import { AppPreview } from "./AppPreview";
import { Header, HEADER_H, HEADER_W } from "./stills/Header";
import { SearchHero, SEARCH_H, SEARCH_W } from "./stills/SearchHero";
import score from "./score.json";
import { FPS, H, W } from "./theme";

export const Root: React.FC = () => (
  <>
    <Composition
      id="AppPreview"
      component={AppPreview}
      durationInFrames={score.durationInFrames}
      fps={FPS}
      width={W}
      height={H}
    />
    <Still id="Header" component={Header} width={HEADER_W} height={HEADER_H} defaultProps={{ guides: false }} />
    <Still id="HeaderGuides" component={Header} width={HEADER_W} height={HEADER_H} defaultProps={{ guides: true }} />
    <Still id="SearchHero" component={SearchHero} width={SEARCH_W} height={SEARCH_H} defaultProps={{ guides: false }} />
    <Still id="SearchHeroGuides" component={SearchHero} width={SEARCH_W} height={SEARCH_H} defaultProps={{ guides: true }} />
  </>
);
