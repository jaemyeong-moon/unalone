import { InputHTMLAttributes, TextareaHTMLAttributes } from 'react';

const inputClass =
  'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent';

interface FormFieldProps {
  label: string;
  hint?: string;
  optional?: boolean;
}

interface InputFieldProps extends FormFieldProps, InputHTMLAttributes<HTMLInputElement> {
  as?: 'input';
}

interface TextareaFieldProps extends FormFieldProps, TextareaHTMLAttributes<HTMLTextAreaElement> {
  as: 'textarea';
}

type Props = InputFieldProps | TextareaFieldProps;

export default function FormField(props: Props) {
  const { label, hint, optional, as: Tag = 'input', ...rest } = props;

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
        {optional && <span className="text-gray-400 font-normal ml-1">(선택)</span>}
      </label>
      {Tag === 'textarea' ? (
        <textarea
          {...(rest as TextareaHTMLAttributes<HTMLTextAreaElement>)}
          className={inputClass}
        />
      ) : (
        <input
          {...(rest as InputHTMLAttributes<HTMLInputElement>)}
          className={inputClass}
        />
      )}
      {hint && <p className="text-xs text-gray-400 mt-1">{hint}</p>}
    </div>
  );
}
