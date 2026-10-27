import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_llm_usage_resolution import GetLlmUsageResolution
from ...models.llm_usage_series import LlmUsageSeries
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    provider: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetLlmUsageResolution | Unset = GetLlmUsageResolution.HOUR,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["provider"] = provider

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    json_resolution: str | Unset = UNSET
    if not isinstance(resolution, Unset):
        json_resolution = resolution.value

    params["resolution"] = json_resolution

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/llm/usage",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmUsageSeries | Problem | None:
    if response.status_code == 200:
        response_200 = LlmUsageSeries.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[LlmUsageSeries | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    provider: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetLlmUsageResolution | Unset = GetLlmUsageResolution.HOUR,
) -> Response[LlmUsageSeries | Problem]:
    """LLM usage over time

     LLM usage over time

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only
    and needs `llm.shared.manage`; personal providers are managed through the provider operations with
    `self.llm`, and the instance roles can be held only by shared providers.

    Args:
        provider (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetLlmUsageResolution | Unset):  Default: GetLlmUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmUsageSeries | Problem]
    """

    kwargs = _get_kwargs(
        provider=provider,
        from_=from_,
        to=to,
        resolution=resolution,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    provider: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetLlmUsageResolution | Unset = GetLlmUsageResolution.HOUR,
) -> LlmUsageSeries | Problem | None:
    """LLM usage over time

     LLM usage over time

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only
    and needs `llm.shared.manage`; personal providers are managed through the provider operations with
    `self.llm`, and the instance roles can be held only by shared providers.

    Args:
        provider (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetLlmUsageResolution | Unset):  Default: GetLlmUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmUsageSeries | Problem
    """

    return sync_detailed(
        client=client,
        provider=provider,
        from_=from_,
        to=to,
        resolution=resolution,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    provider: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetLlmUsageResolution | Unset = GetLlmUsageResolution.HOUR,
) -> Response[LlmUsageSeries | Problem]:
    """LLM usage over time

     LLM usage over time

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only
    and needs `llm.shared.manage`; personal providers are managed through the provider operations with
    `self.llm`, and the instance roles can be held only by shared providers.

    Args:
        provider (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetLlmUsageResolution | Unset):  Default: GetLlmUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmUsageSeries | Problem]
    """

    kwargs = _get_kwargs(
        provider=provider,
        from_=from_,
        to=to,
        resolution=resolution,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    provider: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    resolution: GetLlmUsageResolution | Unset = GetLlmUsageResolution.HOUR,
) -> LlmUsageSeries | Problem | None:
    """LLM usage over time

     LLM usage over time

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only
    and needs `llm.shared.manage`; personal providers are managed through the provider operations with
    `self.llm`, and the instance roles can be held only by shared providers.

    Args:
        provider (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        resolution (GetLlmUsageResolution | Unset):  Default: GetLlmUsageResolution.HOUR.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmUsageSeries | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            provider=provider,
            from_=from_,
            to=to,
            resolution=resolution,
        )
    ).parsed
