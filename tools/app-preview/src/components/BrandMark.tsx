import React from "react";
import { evolvePath } from "@remotion/paths";

export const MARK_PATHS = [
  "M5305.1,2767.8L6117,1414.7l-468.4,-281.6l-662.4,1104.4l-304.3,-507.5c-266.7,-443.9 -842.8,-587.4 -1286.6,-320.7c-131.5,79 -241.7,189.2 -320.7,320.7l-1770.4,2955c-317.5,529 -146.1,1215.2 382.8,1532.8c173.6,104.2 372.3,159.3 574.8,159.3h3232.9c616.6,-1 1116.1,-500.6 1117.1,-1117.1v-10.9c-0.2,-203 -55.6,-402 -160.4,-575.9L5305.1,2767.8z",
  "M3543.8,2014.2c111.4,-184.9 351.6,-244.6 536.5,-133.2c54.6,32.9 100.3,78.6 133.2,133.2l455.6,754.5L3595.7,4554.6H2019.4L3543.8,2014.2z",
  "M4987.1,3299l755.4,1255.6H4233.6L4987.1,3299z",
  "M6065.1,5259.9c0,315 -255.4,570.4 -570.4,570.4l0,0H2261.7c-314.5,0 -569.5,-255 -569.5,-569.5c0,-54 7.7,-107.7 22.8,-159.5h4330c13,47.8 19.8,97.1 20,146.7V5259.9z",
];

const VIEWPORT = 7654;
const GROUP = "translate(1607.34 1607.34) scale(0.58)";

export const BrandMark: React.FC<{
  size: number;
  color: string;
  draw?: number[];
  fill?: number;
  style?: React.CSSProperties;
}> = ({ size, color, draw, fill = 1, style }) => {
  const strokeWidth = (VIEWPORT * 0.0196) / 0.58;
  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${VIEWPORT} ${VIEWPORT}`}
      style={{ overflow: "visible", ...style }}
    >
      <g transform={GROUP}>
        <path
          d={MARK_PATHS.join("")}
          fill={color}
          fillRule="evenodd"
          opacity={fill}
        />
        {draw &&
          MARK_PATHS.map((d, i) => {
            const p = Math.max(0, Math.min(1, draw[i] ?? 0));
            if (p <= 0) return null;
            const { strokeDasharray, strokeDashoffset } = evolvePath(p, d);
            return (
              <path
                key={i}
                d={d}
                fill="none"
                stroke={color}
                strokeWidth={strokeWidth}
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeDasharray={strokeDasharray}
                strokeDashoffset={strokeDashoffset}
                opacity={1 - fill}
              />
            );
          })}
      </g>
    </svg>
  );
};
