from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ntfy_message import NtfyMessage
from ...models.ntfy_publish_via_get_action import NtfyPublishViaGetAction
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    topic: str,
    action: NtfyPublishViaGetAction,
    *,
    message: str | Unset = UNSET,
    title: str | Unset = UNSET,
    priority: str | Unset = UNSET,
    tags: str | Unset = UNSET,
    auth: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["message"] = message

    params["title"] = title

    params["priority"] = priority

    params["tags"] = tags

    params["auth"] = auth

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/compat/ntfy/{topic}/{action}".format(
            topic=quote(str(topic), safe=""),
            action=quote(str(action), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NtfyMessage | Problem | None:
    if response.status_code == 200:
        response_200 = NtfyMessage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NtfyMessage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    topic: str,
    action: NtfyPublishViaGetAction,
    *,
    client: AuthenticatedClient,
    message: str | Unset = UNSET,
    title: str | Unset = UNSET,
    priority: str | Unset = UNSET,
    tags: str | Unset = UNSET,
    auth: str | Unset = UNSET,
) -> Response[NtfyMessage | Problem]:
    """ntfy-compatible publish via GET

     ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the
    ingest key, or `?auth=`. Kind = the source's single kind (else `notification`); payload `{title,
    message, priority, tags, click, topic}`. Outside `/v1`; AccessPolicy rule `/compat/ntfy/**` uses the
    ingest authenticator. Fields from the query string.

    **Scope:** ingest key (or an `admin` token).

    Args:
        topic (str):
        action (NtfyPublishViaGetAction):
        message (str | Unset):
        title (str | Unset):
        priority (str | Unset):
        tags (str | Unset):
        auth (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NtfyMessage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        action=action,
        message=message,
        title=title,
        priority=priority,
        tags=tags,
        auth=auth,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    topic: str,
    action: NtfyPublishViaGetAction,
    *,
    client: AuthenticatedClient,
    message: str | Unset = UNSET,
    title: str | Unset = UNSET,
    priority: str | Unset = UNSET,
    tags: str | Unset = UNSET,
    auth: str | Unset = UNSET,
) -> NtfyMessage | Problem | None:
    """ntfy-compatible publish via GET

     ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the
    ingest key, or `?auth=`. Kind = the source's single kind (else `notification`); payload `{title,
    message, priority, tags, click, topic}`. Outside `/v1`; AccessPolicy rule `/compat/ntfy/**` uses the
    ingest authenticator. Fields from the query string.

    **Scope:** ingest key (or an `admin` token).

    Args:
        topic (str):
        action (NtfyPublishViaGetAction):
        message (str | Unset):
        title (str | Unset):
        priority (str | Unset):
        tags (str | Unset):
        auth (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NtfyMessage | Problem
    """

    return sync_detailed(
        topic=topic,
        action=action,
        client=client,
        message=message,
        title=title,
        priority=priority,
        tags=tags,
        auth=auth,
    ).parsed


async def asyncio_detailed(
    topic: str,
    action: NtfyPublishViaGetAction,
    *,
    client: AuthenticatedClient,
    message: str | Unset = UNSET,
    title: str | Unset = UNSET,
    priority: str | Unset = UNSET,
    tags: str | Unset = UNSET,
    auth: str | Unset = UNSET,
) -> Response[NtfyMessage | Problem]:
    """ntfy-compatible publish via GET

     ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the
    ingest key, or `?auth=`. Kind = the source's single kind (else `notification`); payload `{title,
    message, priority, tags, click, topic}`. Outside `/v1`; AccessPolicy rule `/compat/ntfy/**` uses the
    ingest authenticator. Fields from the query string.

    **Scope:** ingest key (or an `admin` token).

    Args:
        topic (str):
        action (NtfyPublishViaGetAction):
        message (str | Unset):
        title (str | Unset):
        priority (str | Unset):
        tags (str | Unset):
        auth (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NtfyMessage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        action=action,
        message=message,
        title=title,
        priority=priority,
        tags=tags,
        auth=auth,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    topic: str,
    action: NtfyPublishViaGetAction,
    *,
    client: AuthenticatedClient,
    message: str | Unset = UNSET,
    title: str | Unset = UNSET,
    priority: str | Unset = UNSET,
    tags: str | Unset = UNSET,
    auth: str | Unset = UNSET,
) -> NtfyMessage | Problem | None:
    """ntfy-compatible publish via GET

     ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the
    ingest key, or `?auth=`. Kind = the source's single kind (else `notification`); payload `{title,
    message, priority, tags, click, topic}`. Outside `/v1`; AccessPolicy rule `/compat/ntfy/**` uses the
    ingest authenticator. Fields from the query string.

    **Scope:** ingest key (or an `admin` token).

    Args:
        topic (str):
        action (NtfyPublishViaGetAction):
        message (str | Unset):
        title (str | Unset):
        priority (str | Unset):
        tags (str | Unset):
        auth (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NtfyMessage | Problem
    """

    return (
        await asyncio_detailed(
            topic=topic,
            action=action,
            client=client,
            message=message,
            title=title,
            priority=priority,
            tags=tags,
            auth=auth,
        )
    ).parsed
