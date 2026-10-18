from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_vapid_public_key_response_200 import GetVapidPublicKeyResponse200
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/push/vapid-public-key",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> GetVapidPublicKeyResponse200 | Problem | None:
    if response.status_code == 200:
        response_200 = GetVapidPublicKeyResponse200.from_dict(response.json())

        return response_200

    if 400 <= response.status_code <= 499:
        response_4xx = Problem.from_dict(response.json())

        return response_4xx

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[GetVapidPublicKeyResponse200 | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient | Client,
) -> Response[GetVapidPublicKeyResponse200 | Problem]:
    """VAPID public key for Web Push subscriptions

     VAPID public key for Web Push subscriptions: the base64url uncompressed P-256 point (87 characters)
    Foresignal generated on first start. Browsers pass it as `applicationServerKey` to
    `pushManager.subscribe`; UnifiedPush apps pass it to `registerApp(vapid: …)`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetVapidPublicKeyResponse200 | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient | Client,
) -> GetVapidPublicKeyResponse200 | Problem | None:
    """VAPID public key for Web Push subscriptions

     VAPID public key for Web Push subscriptions: the base64url uncompressed P-256 point (87 characters)
    Foresignal generated on first start. Browsers pass it as `applicationServerKey` to
    `pushManager.subscribe`; UnifiedPush apps pass it to `registerApp(vapid: …)`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetVapidPublicKeyResponse200 | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient | Client,
) -> Response[GetVapidPublicKeyResponse200 | Problem]:
    """VAPID public key for Web Push subscriptions

     VAPID public key for Web Push subscriptions: the base64url uncompressed P-256 point (87 characters)
    Foresignal generated on first start. Browsers pass it as `applicationServerKey` to
    `pushManager.subscribe`; UnifiedPush apps pass it to `registerApp(vapid: …)`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetVapidPublicKeyResponse200 | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient | Client,
) -> GetVapidPublicKeyResponse200 | Problem | None:
    """VAPID public key for Web Push subscriptions

     VAPID public key for Web Push subscriptions: the base64url uncompressed P-256 point (87 characters)
    Foresignal generated on first start. Browsers pass it as `applicationServerKey` to
    `pushManager.subscribe`; UnifiedPush apps pass it to `registerApp(vapid: …)`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetVapidPublicKeyResponse200 | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
