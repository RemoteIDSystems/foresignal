from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.hypothesis_review_input import HypothesisReviewInput
from ...models.hypothesis_revision import HypothesisRevision
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    set_id: str,
    *,
    body: HypothesisReviewInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/hypotheses/sets/{set_id}/review".format(
            set_id=quote(str(set_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> HypothesisRevision | Problem | None:
    if response.status_code == 201:
        response_201 = HypothesisRevision.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[HypothesisRevision | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisReviewInput,
) -> Response[HypothesisRevision | Problem]:
    """Record a hypothesis set review

     Records a revision with cause `review` (or `operator`) and a rationale, or confirms an analyst
    proposal. A review may change LRs, assumptions or probabilities directly; overrides above a
    speculative-link cap are logged and labeled.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisReviewInput): A review revision (cause `review` or `operator`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisRevision | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    set_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisReviewInput,
) -> HypothesisRevision | Problem | None:
    """Record a hypothesis set review

     Records a revision with cause `review` (or `operator`) and a rationale, or confirms an analyst
    proposal. A review may change LRs, assumptions or probabilities directly; overrides above a
    speculative-link cap are logged and labeled.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisReviewInput): A review revision (cause `review` or `operator`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisRevision | Problem
    """

    return sync_detailed(
        set_id=set_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisReviewInput,
) -> Response[HypothesisRevision | Problem]:
    """Record a hypothesis set review

     Records a revision with cause `review` (or `operator`) and a rationale, or confirms an analyst
    proposal. A review may change LRs, assumptions or probabilities directly; overrides above a
    speculative-link cap are logged and labeled.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisReviewInput): A review revision (cause `review` or `operator`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisRevision | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    set_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisReviewInput,
) -> HypothesisRevision | Problem | None:
    """Record a hypothesis set review

     Records a revision with cause `review` (or `operator`) and a rationale, or confirms an analyst
    proposal. A review may change LRs, assumptions or probabilities directly; overrides above a
    speculative-link cap are logged and labeled.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisReviewInput): A review revision (cause `review` or `operator`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisRevision | Problem
    """

    return (
        await asyncio_detailed(
            set_id=set_id,
            client=client,
            body=body,
        )
    ).parsed
