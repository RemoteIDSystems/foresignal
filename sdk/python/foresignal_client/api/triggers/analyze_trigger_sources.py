from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.source_relevance_analysis import SourceRelevanceAnalysis
from ...models.source_relevance_request import SourceRelevanceRequest
from ...types import Response


def _get_kwargs(
    *,
    body: SourceRelevanceRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/triggers/analyze-sources",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SourceRelevanceAnalysis | None:
    if response.status_code == 200:
        response_200 = SourceRelevanceAnalysis.from_dict(response.json())

        return response_200

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 502:
        response_502 = Problem.from_dict(response.json())

        return response_502

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SourceRelevanceAnalysis]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SourceRelevanceRequest,
) -> Response[Problem | SourceRelevanceAnalysis]:
    """Rate how much each source contributes to a rule's dataset (advisory)

     Asks the chat provider, through the versioned template `source-relevance-system@1.0.0` and the
    analyst purpose, how much each of 2 to 50 sources contributes to a dataset built from all of them,
    with a one-sentence reason per source. The model sees only catalog content, as an untrusted data
    block: each source's name, description, kinds and entity identity types, and the kinds and identity
    types two or more of them share (`shared`). The answer is advice: nothing is enabled, omitted or
    stored, and the caller decides which sources the rule keeps. Every requested source comes back
    exactly once, in request order. A source the egress policy keeps from the provider (a private source
    and a remote provider) is not sent and comes back `unrated`, as does one the model left out or rated
    with another value; when fewer than two sources may be sent, or the answer cannot be read, every
    source is `unrated` with the reason. Spends LLM budget, so it always needs a token.

    **Scope:** `agent`.

    Args:
        body (SourceRelevanceRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceRelevanceAnalysis]
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
    body: SourceRelevanceRequest,
) -> Problem | SourceRelevanceAnalysis | None:
    """Rate how much each source contributes to a rule's dataset (advisory)

     Asks the chat provider, through the versioned template `source-relevance-system@1.0.0` and the
    analyst purpose, how much each of 2 to 50 sources contributes to a dataset built from all of them,
    with a one-sentence reason per source. The model sees only catalog content, as an untrusted data
    block: each source's name, description, kinds and entity identity types, and the kinds and identity
    types two or more of them share (`shared`). The answer is advice: nothing is enabled, omitted or
    stored, and the caller decides which sources the rule keeps. Every requested source comes back
    exactly once, in request order. A source the egress policy keeps from the provider (a private source
    and a remote provider) is not sent and comes back `unrated`, as does one the model left out or rated
    with another value; when fewer than two sources may be sent, or the answer cannot be read, every
    source is `unrated` with the reason. Spends LLM budget, so it always needs a token.

    **Scope:** `agent`.

    Args:
        body (SourceRelevanceRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceRelevanceAnalysis
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SourceRelevanceRequest,
) -> Response[Problem | SourceRelevanceAnalysis]:
    """Rate how much each source contributes to a rule's dataset (advisory)

     Asks the chat provider, through the versioned template `source-relevance-system@1.0.0` and the
    analyst purpose, how much each of 2 to 50 sources contributes to a dataset built from all of them,
    with a one-sentence reason per source. The model sees only catalog content, as an untrusted data
    block: each source's name, description, kinds and entity identity types, and the kinds and identity
    types two or more of them share (`shared`). The answer is advice: nothing is enabled, omitted or
    stored, and the caller decides which sources the rule keeps. Every requested source comes back
    exactly once, in request order. A source the egress policy keeps from the provider (a private source
    and a remote provider) is not sent and comes back `unrated`, as does one the model left out or rated
    with another value; when fewer than two sources may be sent, or the answer cannot be read, every
    source is `unrated` with the reason. Spends LLM budget, so it always needs a token.

    **Scope:** `agent`.

    Args:
        body (SourceRelevanceRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SourceRelevanceAnalysis]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SourceRelevanceRequest,
) -> Problem | SourceRelevanceAnalysis | None:
    """Rate how much each source contributes to a rule's dataset (advisory)

     Asks the chat provider, through the versioned template `source-relevance-system@1.0.0` and the
    analyst purpose, how much each of 2 to 50 sources contributes to a dataset built from all of them,
    with a one-sentence reason per source. The model sees only catalog content, as an untrusted data
    block: each source's name, description, kinds and entity identity types, and the kinds and identity
    types two or more of them share (`shared`). The answer is advice: nothing is enabled, omitted or
    stored, and the caller decides which sources the rule keeps. Every requested source comes back
    exactly once, in request order. A source the egress policy keeps from the provider (a private source
    and a remote provider) is not sent and comes back `unrated`, as does one the model left out or rated
    with another value; when fewer than two sources may be sent, or the answer cannot be read, every
    source is `unrated` with the reason. Spends LLM budget, so it always needs a token.

    **Scope:** `agent`.

    Args:
        body (SourceRelevanceRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SourceRelevanceAnalysis
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
