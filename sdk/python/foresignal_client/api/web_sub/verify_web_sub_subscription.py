from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.verify_web_sub_subscription_hub_mode import (
    VerifyWebSubSubscriptionHubMode,
)
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    token: str,
    *,
    hub_mode: VerifyWebSubSubscriptionHubMode,
    hub_topic: str,
    hub_challenge: str | Unset = UNSET,
    hub_lease_seconds: int | Unset = UNSET,
    hub_reason: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_hub_mode = hub_mode.value
    params["hub.mode"] = json_hub_mode

    params["hub.topic"] = hub_topic

    params["hub.challenge"] = hub_challenge

    params["hub.lease_seconds"] = hub_lease_seconds

    params["hub.reason"] = hub_reason

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/websub/{source_id}/{token}".format(
            source_id=quote(str(source_id), safe=""),
            token=quote(str(token), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    hub_mode: VerifyWebSubSubscriptionHubMode,
    hub_topic: str,
    hub_challenge: str | Unset = UNSET,
    hub_lease_seconds: int | Unset = UNSET,
    hub_reason: str | Unset = UNSET,
) -> Response[Problem | str]:
    """WebSub intent verification

     Hub intent verification. Echoes `hub.challenge` as `text/plain` only when `hub.mode=subscribe`,
    `hub.topic` and the token match a pending or active subscription; the lease (`hub.lease_seconds`,
    default 86400) starts then. `hub.mode=denied` with a matching topic and token marks the subscription
    lost and answers 200 with an empty body. Everything else, `unsubscribe` included (Foresignal never
    unsubscribes), is 404. Returns 404 for everything while `foresignal.kits.websub.enabled=false`
    (default).

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        hub_mode (VerifyWebSubSubscriptionHubMode):
        hub_topic (str):
        hub_challenge (str | Unset):
        hub_lease_seconds (int | Unset):
        hub_reason (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        token=token,
        hub_mode=hub_mode,
        hub_topic=hub_topic,
        hub_challenge=hub_challenge,
        hub_lease_seconds=hub_lease_seconds,
        hub_reason=hub_reason,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    hub_mode: VerifyWebSubSubscriptionHubMode,
    hub_topic: str,
    hub_challenge: str | Unset = UNSET,
    hub_lease_seconds: int | Unset = UNSET,
    hub_reason: str | Unset = UNSET,
) -> Problem | str | None:
    """WebSub intent verification

     Hub intent verification. Echoes `hub.challenge` as `text/plain` only when `hub.mode=subscribe`,
    `hub.topic` and the token match a pending or active subscription; the lease (`hub.lease_seconds`,
    default 86400) starts then. `hub.mode=denied` with a matching topic and token marks the subscription
    lost and answers 200 with an empty body. Everything else, `unsubscribe` included (Foresignal never
    unsubscribes), is 404. Returns 404 for everything while `foresignal.kits.websub.enabled=false`
    (default).

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        hub_mode (VerifyWebSubSubscriptionHubMode):
        hub_topic (str):
        hub_challenge (str | Unset):
        hub_lease_seconds (int | Unset):
        hub_reason (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return sync_detailed(
        source_id=source_id,
        token=token,
        client=client,
        hub_mode=hub_mode,
        hub_topic=hub_topic,
        hub_challenge=hub_challenge,
        hub_lease_seconds=hub_lease_seconds,
        hub_reason=hub_reason,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    hub_mode: VerifyWebSubSubscriptionHubMode,
    hub_topic: str,
    hub_challenge: str | Unset = UNSET,
    hub_lease_seconds: int | Unset = UNSET,
    hub_reason: str | Unset = UNSET,
) -> Response[Problem | str]:
    """WebSub intent verification

     Hub intent verification. Echoes `hub.challenge` as `text/plain` only when `hub.mode=subscribe`,
    `hub.topic` and the token match a pending or active subscription; the lease (`hub.lease_seconds`,
    default 86400) starts then. `hub.mode=denied` with a matching topic and token marks the subscription
    lost and answers 200 with an empty body. Everything else, `unsubscribe` included (Foresignal never
    unsubscribes), is 404. Returns 404 for everything while `foresignal.kits.websub.enabled=false`
    (default).

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        hub_mode (VerifyWebSubSubscriptionHubMode):
        hub_topic (str):
        hub_challenge (str | Unset):
        hub_lease_seconds (int | Unset):
        hub_reason (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        token=token,
        hub_mode=hub_mode,
        hub_topic=hub_topic,
        hub_challenge=hub_challenge,
        hub_lease_seconds=hub_lease_seconds,
        hub_reason=hub_reason,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    hub_mode: VerifyWebSubSubscriptionHubMode,
    hub_topic: str,
    hub_challenge: str | Unset = UNSET,
    hub_lease_seconds: int | Unset = UNSET,
    hub_reason: str | Unset = UNSET,
) -> Problem | str | None:
    """WebSub intent verification

     Hub intent verification. Echoes `hub.challenge` as `text/plain` only when `hub.mode=subscribe`,
    `hub.topic` and the token match a pending or active subscription; the lease (`hub.lease_seconds`,
    default 86400) starts then. `hub.mode=denied` with a matching topic and token marks the subscription
    lost and answers 200 with an empty body. Everything else, `unsubscribe` included (Foresignal never
    unsubscribes), is 404. Returns 404 for everything while `foresignal.kits.websub.enabled=false`
    (default).

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        hub_mode (VerifyWebSubSubscriptionHubMode):
        hub_topic (str):
        hub_challenge (str | Unset):
        hub_lease_seconds (int | Unset):
        hub_reason (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            token=token,
            client=client,
            hub_mode=hub_mode,
            hub_topic=hub_topic,
            hub_challenge=hub_challenge,
            hub_lease_seconds=hub_lease_seconds,
            hub_reason=hub_reason,
        )
    ).parsed
