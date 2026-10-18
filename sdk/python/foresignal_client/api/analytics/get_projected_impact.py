from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.projected_impact import ProjectedImpact
from ...types import Response


def _get_kwargs(
    finding_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/impacts/{finding_id}".format(
            finding_id=quote(str(finding_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ProjectedImpact | None:
    if response.status_code == 200:
        response_200 = ProjectedImpact.from_dict(response.json())

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
) -> Response[Problem | ProjectedImpact]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    finding_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ProjectedImpact]:
    """Get a projected impact

     One projection with its hazard input, conditional rate, exposed assets, dependency paths, labels and
    ledger question.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProjectedImpact]
    """

    kwargs = _get_kwargs(
        finding_id=finding_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    finding_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ProjectedImpact | None:
    """Get a projected impact

     One projection with its hazard input, conditional rate, exposed assets, dependency paths, labels and
    ledger question.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProjectedImpact
    """

    return sync_detailed(
        finding_id=finding_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    finding_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ProjectedImpact]:
    """Get a projected impact

     One projection with its hazard input, conditional rate, exposed assets, dependency paths, labels and
    ledger question.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProjectedImpact]
    """

    kwargs = _get_kwargs(
        finding_id=finding_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    finding_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ProjectedImpact | None:
    """Get a projected impact

     One projection with its hazard input, conditional rate, exposed assets, dependency paths, labels and
    ledger question.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProjectedImpact
    """

    return (
        await asyncio_detailed(
            finding_id=finding_id,
            client=client,
        )
    ).parsed
