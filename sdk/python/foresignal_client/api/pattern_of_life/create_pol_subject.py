from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.pol_subject import PolSubject
from ...models.pol_subject_input import PolSubjectInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PolSubjectInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/pol/subjects",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PolSubject | Problem | None:
    if response.status_code == 201:
        response_201 = PolSubject.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[PolSubject | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PolSubjectInput,
) -> Response[PolSubject | Problem]:
    """Opt a subject into pattern of life

     Opts a record, entity or watchlist in. Stays are extracted incrementally and clustered nightly
    (DBSCAN); places and deviations (NEW_PLACE, UNUSUAL_TIME, EXPECTED_ABSENT) are published on the
    derived source `pattern-of-life`. Subjects on private sources need admin and inherit private and the
    precision cap.

    **Scope:** `agent`.

    Args:
        body (PolSubjectInput): Opt a subject into pattern-of-life analysis (114). Private sources
            need admin

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PolSubject | Problem]
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
    body: PolSubjectInput,
) -> PolSubject | Problem | None:
    """Opt a subject into pattern of life

     Opts a record, entity or watchlist in. Stays are extracted incrementally and clustered nightly
    (DBSCAN); places and deviations (NEW_PLACE, UNUSUAL_TIME, EXPECTED_ABSENT) are published on the
    derived source `pattern-of-life`. Subjects on private sources need admin and inherit private and the
    precision cap.

    **Scope:** `agent`.

    Args:
        body (PolSubjectInput): Opt a subject into pattern-of-life analysis (114). Private sources
            need admin

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PolSubject | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PolSubjectInput,
) -> Response[PolSubject | Problem]:
    """Opt a subject into pattern of life

     Opts a record, entity or watchlist in. Stays are extracted incrementally and clustered nightly
    (DBSCAN); places and deviations (NEW_PLACE, UNUSUAL_TIME, EXPECTED_ABSENT) are published on the
    derived source `pattern-of-life`. Subjects on private sources need admin and inherit private and the
    precision cap.

    **Scope:** `agent`.

    Args:
        body (PolSubjectInput): Opt a subject into pattern-of-life analysis (114). Private sources
            need admin

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PolSubject | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PolSubjectInput,
) -> PolSubject | Problem | None:
    """Opt a subject into pattern of life

     Opts a record, entity or watchlist in. Stays are extracted incrementally and clustered nightly
    (DBSCAN); places and deviations (NEW_PLACE, UNUSUAL_TIME, EXPECTED_ABSENT) are published on the
    derived source `pattern-of-life`. Subjects on private sources need admin and inherit private and the
    precision cap.

    **Scope:** `agent`.

    Args:
        body (PolSubjectInput): Opt a subject into pattern-of-life analysis (114). Private sources
            need admin

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PolSubject | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
