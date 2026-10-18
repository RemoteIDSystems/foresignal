from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.attention_topic import AttentionTopic
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    topic_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/attention/topics/{topic_id}".format(
            topic_id=quote(str(topic_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AttentionTopic | Problem | None:
    if response.status_code == 200:
        response_200 = AttentionTopic.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AttentionTopic | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    topic_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AttentionTopic | Problem]:
    """Get an attention topic

     Errors: `404 UNKNOWN_ATTENTION_TOPIC`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopic | Problem]
    """

    kwargs = _get_kwargs(
        topic_id=topic_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    topic_id: str,
    *,
    client: AuthenticatedClient,
) -> AttentionTopic | Problem | None:
    """Get an attention topic

     Errors: `404 UNKNOWN_ATTENTION_TOPIC`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopic | Problem
    """

    return sync_detailed(
        topic_id=topic_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    topic_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AttentionTopic | Problem]:
    """Get an attention topic

     Errors: `404 UNKNOWN_ATTENTION_TOPIC`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopic | Problem]
    """

    kwargs = _get_kwargs(
        topic_id=topic_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    topic_id: str,
    *,
    client: AuthenticatedClient,
) -> AttentionTopic | Problem | None:
    """Get an attention topic

     Errors: `404 UNKNOWN_ATTENTION_TOPIC`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopic | Problem
    """

    return (
        await asyncio_detailed(
            topic_id=topic_id,
            client=client,
        )
    ).parsed
