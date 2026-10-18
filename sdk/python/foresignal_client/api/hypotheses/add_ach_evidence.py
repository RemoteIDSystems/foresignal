from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ach_evidence import AchEvidence
from ...models.ach_evidence_input import AchEvidenceInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    set_id: str,
    *,
    body: AchEvidenceInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/hypotheses/sets/{set_id}/evidence".format(
            set_id=quote(str(set_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AchEvidence | Problem | None:
    if response.status_code == 201:
        response_201 = AchEvidence.from_dict(response.json())

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
) -> Response[AchEvidence | Problem]:
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
    body: AchEvidenceInput,
) -> Response[AchEvidence | Problem]:
    """Add ACH evidence

     Adds an evidence row (re-reports attach to the existing row of their 116 evidence group) and records
    an `evidence-added` revision when it carries likelihood.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AchEvidenceInput): Manual or referenced evidence to add to a set's ACH matrix

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AchEvidence | Problem]
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
    body: AchEvidenceInput,
) -> AchEvidence | Problem | None:
    """Add ACH evidence

     Adds an evidence row (re-reports attach to the existing row of their 116 evidence group) and records
    an `evidence-added` revision when it carries likelihood.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AchEvidenceInput): Manual or referenced evidence to add to a set's ACH matrix

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AchEvidence | Problem
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
    body: AchEvidenceInput,
) -> Response[AchEvidence | Problem]:
    """Add ACH evidence

     Adds an evidence row (re-reports attach to the existing row of their 116 evidence group) and records
    an `evidence-added` revision when it carries likelihood.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AchEvidenceInput): Manual or referenced evidence to add to a set's ACH matrix

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AchEvidence | Problem]
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
    body: AchEvidenceInput,
) -> AchEvidence | Problem | None:
    """Add ACH evidence

     Adds an evidence row (re-reports attach to the existing row of their 116 evidence group) and records
    an `evidence-added` revision when it carries likelihood.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AchEvidenceInput): Manual or referenced evidence to add to a set's ACH matrix

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AchEvidence | Problem
    """

    return (
        await asyncio_detailed(
            set_id=set_id,
            client=client,
            body=body,
        )
    ).parsed
