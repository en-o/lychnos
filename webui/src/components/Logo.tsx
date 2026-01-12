import React from 'react';
import { getAssetUrl } from '../utils/assets';

interface LogoProps {
  className?: string;
}

const Logo: React.FC<LogoProps> = ({ className = 'w-8 h-8' }) => {
  return (
    <img
      src={getAssetUrl('site_icon_64.svg')}
      alt="书灯"
      className={className}
    />
  );
};

export default Logo;
