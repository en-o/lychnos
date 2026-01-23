import React from 'react';
import {toast} from './ToastContainer';

interface CopyableTextProps {
    text: string;
    maxWidth?: string;
    className?: string;
}

const CopyableText: React.FC<CopyableTextProps> = ({
    text,
    maxWidth = '150px',
    className = '',
}) => {
    const handleDoubleClick = async () => {
        try {
            await navigator.clipboard.writeText(text);
            toast.success('已复制到剪贴板');
        } catch (error) {
            toast.error('复制失败');
        }
    };

    return (
        <div
            className={`truncate cursor-pointer hover:text-blue-600 transition ${className}`}
            style={{ maxWidth }}
            title={`${text}\n\n双击复制`}
            onDoubleClick={handleDoubleClick}
        >
            {text}
        </div>
    );
};

export default CopyableText;
