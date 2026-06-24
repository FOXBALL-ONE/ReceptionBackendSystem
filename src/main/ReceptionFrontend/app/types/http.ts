export interface ApiResult<T> {
    code?: string | number;
    msg?: string;
    status?: string | number;
    message?: string;
    data: T;
}
