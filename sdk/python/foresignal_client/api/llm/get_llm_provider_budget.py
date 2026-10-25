from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_provider_budget import LlmProviderBudget
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    provider_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/llm/providers/{provider_id}/budget".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmProviderBudget | Problem | None:
    if response.status_code == 200:
        response_200 = LlmProviderBudget.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[LlmProviderBudget | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[LlmProviderBudget | Problem]:
    """A provider's spend against its USD caps

     Per window (the UTC hour, day and month) the USD spent, the estimated USD of calls in flight, the
    cap, what remains and when the window resets; the token and request limits; and, for providers whose
    API reports one to the same key, the provider-reported balance (DeepSeek, OpenRouter), shown for
    information and never enforced. Spend is Foresignal's own metered tokens at the provider's
    `pricing`.

    **Scope:** `admin`.

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProviderBudget | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> LlmProviderBudget | Problem | None:
    """A provider's spend against its USD caps

     Per window (the UTC hour, day and month) the USD spent, the estimated USD of calls in flight, the
    cap, what remains and when the window resets; the token and request limits; and, for providers whose
    API reports one to the same key, the provider-reported balance (DeepSeek, OpenRouter), shown for
    information and never enforced. Spend is Foresignal's own metered tokens at the provider's
    `pricing`.

    **Scope:** `admin`.

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProviderBudget | Problem
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[LlmProviderBudget | Problem]:
    """A provider's spend against its USD caps

     Per window (the UTC hour, day and month) the USD spent, the estimated USD of calls in flight, the
    cap, what remains and when the window resets; the token and request limits; and, for providers whose
    API reports one to the same key, the provider-reported balance (DeepSeek, OpenRouter), shown for
    information and never enforced. Spend is Foresignal's own metered tokens at the provider's
    `pricing`.

    **Scope:** `admin`.

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProviderBudget | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> LlmProviderBudget | Problem | None:
    """A provider's spend against its USD caps

     Per window (the UTC hour, day and month) the USD spent, the estimated USD of calls in flight, the
    cap, what remains and when the window resets; the token and request limits; and, for providers whose
    API reports one to the same key, the provider-reported balance (DeepSeek, OpenRouter), shown for
    information and never enforced. Spend is Foresignal's own metered tokens at the provider's
    `pricing`.

    **Scope:** `admin`.

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProviderBudget | Problem
    """

    return (
        await asyncio_detailed(
            provider_id=provider_id,
            client=client,
        )
    ).parsed
