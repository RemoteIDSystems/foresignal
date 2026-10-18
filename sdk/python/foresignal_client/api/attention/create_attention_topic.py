from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.attention_topic import AttentionTopic
from ...models.attention_topic_input import AttentionTopicInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: AttentionTopicInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/attention/topics",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AttentionTopic | Problem | None:
    if response.status_code == 201:
        response_201 = AttentionTopic.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

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
    *,
    client: AuthenticatedClient,
    body: AttentionTopicInput,
) -> Response[AttentionTopic | Problem]:
    """Create an attention topic

     Validates the GDELT query (character check and, when gdelt-timelines is enabled, one budgeted test
    call) and syncs enabled topics into the `topics` params of gdelt-timelines and wikimedia-pageviews.
    Errors: `400 TOPIC_INVALID`.

    **Scope:** `agent`.

    Args:
        body (AttentionTopicInput): Create or replace an attention topic (117)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopic | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: AttentionTopicInput,
) -> AttentionTopic | Problem | None:
    """Create an attention topic

     Validates the GDELT query (character check and, when gdelt-timelines is enabled, one budgeted test
    call) and syncs enabled topics into the `topics` params of gdelt-timelines and wikimedia-pageviews.
    Errors: `400 TOPIC_INVALID`.

    **Scope:** `agent`.

    Args:
        body (AttentionTopicInput): Create or replace an attention topic (117)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopic | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: AttentionTopicInput,
) -> Response[AttentionTopic | Problem]:
    """Create an attention topic

     Validates the GDELT query (character check and, when gdelt-timelines is enabled, one budgeted test
    call) and syncs enabled topics into the `topics` params of gdelt-timelines and wikimedia-pageviews.
    Errors: `400 TOPIC_INVALID`.

    **Scope:** `agent`.

    Args:
        body (AttentionTopicInput): Create or replace an attention topic (117)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopic | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: AttentionTopicInput,
) -> AttentionTopic | Problem | None:
    """Create an attention topic

     Validates the GDELT query (character check and, when gdelt-timelines is enabled, one budgeted test
    call) and syncs enabled topics into the `topics` params of gdelt-timelines and wikimedia-pageviews.
    Errors: `400 TOPIC_INVALID`.

    **Scope:** `agent`.

    Args:
        body (AttentionTopicInput): Create or replace an attention topic (117)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopic | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
