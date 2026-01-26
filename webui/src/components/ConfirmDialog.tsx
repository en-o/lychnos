import React from 'react';
import {AlertTriangle, CheckCircle, Info, XCircle} from 'lucide-react';

export type ConfirmDialogType = 'warning' | 'danger' | 'info' | 'success';

interface ConfirmDialogProps {
    isOpen: boolean;
    title: string;
    message: string;
    type?: ConfirmDialogType;
    confirmText?: string;
    cancelText?: string;
    onConfirm: () => void;
    onCancel: () => void;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
    isOpen,
    title,
    message,
    type = 'warning',
    confirmText = '确定',
    cancelText = '取消',
    onConfirm,
    onCancel,
}) => {
    if (!isOpen) return null;

    const getTypeStyles = () => {
        switch (type) {
            case 'danger':
                return {
                    icon: <XCircle className="w-12 h-12 text-red-500" />,
                    iconBg: 'bg-red-50',
                    confirmBtn: 'bg-red-600 hover:bg-red-700 text-white',
                };
            case 'warning':
                return {
                    icon: <AlertTriangle className="w-12 h-12 text-yellow-500" />,
                    iconBg: 'bg-yellow-50',
                    confirmBtn: 'bg-yellow-600 hover:bg-yellow-700 text-white',
                };
            case 'info':
                return {
                    icon: <Info className="w-12 h-12 text-blue-500" />,
                    iconBg: 'bg-blue-50',
                    confirmBtn: 'bg-blue-600 hover:bg-blue-700 text-white',
                };
            case 'success':
                return {
                    icon: <CheckCircle className="w-12 h-12 text-green-500" />,
                    iconBg: 'bg-green-50',
                    confirmBtn: 'bg-green-600 hover:bg-green-700 text-white',
                };
        }
    };

    const styles = getTypeStyles();

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* 背景遮罩 */}
            <div
                className="absolute inset-0 bg-black bg-opacity-50 transition-opacity"
                onClick={onCancel}
            />

            {/* 对话框 */}
            <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 transform transition-all">
                {/* 图标 */}
                <div className="flex items-center justify-center pt-6 pb-4">
                    <div className={`${styles.iconBg} rounded-full p-3`}>
                        {styles.icon}
                    </div>
                </div>

                {/* 内容 */}
                <div className="px-6 pb-6">
                    <h3 className="text-lg font-semibold text-gray-900 text-center mb-2">
                        {title}
                    </h3>
                    <p className="text-sm text-gray-600 text-center whitespace-pre-line">
                        {message}
                    </p>
                </div>

                {/* 按钮 */}
                <div className="flex gap-3 px-6 pb-6">
                    <button
                        onClick={onCancel}
                        className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium"
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={onConfirm}
                        className={`flex-1 px-4 py-2.5 rounded-lg transition font-medium ${styles.confirmBtn}`}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmDialog;
