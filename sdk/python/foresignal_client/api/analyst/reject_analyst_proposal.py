from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analyst_proposal import AnalystProposal
from ...models.analyst_proposal_decision import AnalystProposalDecision
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    proposal_id: str,
    *,
    body: AnalystProposalDecision | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/analyst/proposals/{proposal_id}/reject".format(
            proposal_id=quote(str(proposal_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalystProposal | Problem | None:
    if response.status_code == 200:
        response_200 = AnalystProposal.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalystProposal | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    proposal_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystProposalDecision | Unset = UNSET,
) -> Response[AnalystProposal | Problem]:
    """Reject a pending proposal

     Marks the proposal rejected.

    **Scope:** `agent` or `admin`.

    Args:
        proposal_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystProposalDecision | Unset): Optional reason for a rejection

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystProposal | Problem]
    """

    kwargs = _get_kwargs(
        proposal_id=proposal_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    proposal_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystProposalDecision | Unset = UNSET,
) -> AnalystProposal | Problem | None:
    """Reject a pending proposal

     Marks the proposal rejected.

    **Scope:** `agent` or `admin`.

    Args:
        proposal_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystProposalDecision | Unset): Optional reason for a rejection

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystProposal | Problem
    """

    return sync_detailed(
        proposal_id=proposal_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    proposal_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystProposalDecision | Unset = UNSET,
) -> Response[AnalystProposal | Problem]:
    """Reject a pending proposal

     Marks the proposal rejected.

    **Scope:** `agent` or `admin`.

    Args:
        proposal_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystProposalDecision | Unset): Optional reason for a rejection

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystProposal | Problem]
    """

    kwargs = _get_kwargs(
        proposal_id=proposal_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    proposal_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystProposalDecision | Unset = UNSET,
) -> AnalystProposal | Problem | None:
    """Reject a pending proposal

     Marks the proposal rejected.

    **Scope:** `agent` or `admin`.

    Args:
        proposal_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystProposalDecision | Unset): Optional reason for a rejection

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystProposal | Problem
    """

    return (
        await asyncio_detailed(
            proposal_id=proposal_id,
            client=client,
            body=body,
        )
    ).parsed
