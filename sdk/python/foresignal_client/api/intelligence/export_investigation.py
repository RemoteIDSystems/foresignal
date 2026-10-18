from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.export_investigation_format import ExportInvestigationFormat
from ...models.investigation_export import InvestigationExport
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    investigation_id: str,
    *,
    format_: ExportInvestigationFormat | Unset = ExportInvestigationFormat.JSON,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/investigations/{investigation_id}/export".format(
            investigation_id=quote(str(investigation_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InvestigationExport | Problem | None:
    if response.status_code == 200:
        response_200 = InvestigationExport.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[InvestigationExport | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    format_: ExportInvestigationFormat | Unset = ExportInvestigationFormat.JSON,
) -> Response[InvestigationExport | Problem]:
    """Export an investigation

     JSON, Markdown report with citations `[source/externalId@vN]`, or GeoJSON. Every format carries the
    header "Automated analysis inputs; verify before acting" and licence attributions.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        investigation_id (str):
        format_ (ExportInvestigationFormat | Unset):  Default: ExportInvestigationFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationExport | Problem]
    """

    kwargs = _get_kwargs(
        investigation_id=investigation_id,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    format_: ExportInvestigationFormat | Unset = ExportInvestigationFormat.JSON,
) -> InvestigationExport | Problem | None:
    """Export an investigation

     JSON, Markdown report with citations `[source/externalId@vN]`, or GeoJSON. Every format carries the
    header "Automated analysis inputs; verify before acting" and licence attributions.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        investigation_id (str):
        format_ (ExportInvestigationFormat | Unset):  Default: ExportInvestigationFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationExport | Problem
    """

    return sync_detailed(
        investigation_id=investigation_id,
        client=client,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    format_: ExportInvestigationFormat | Unset = ExportInvestigationFormat.JSON,
) -> Response[InvestigationExport | Problem]:
    """Export an investigation

     JSON, Markdown report with citations `[source/externalId@vN]`, or GeoJSON. Every format carries the
    header "Automated analysis inputs; verify before acting" and licence attributions.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        investigation_id (str):
        format_ (ExportInvestigationFormat | Unset):  Default: ExportInvestigationFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationExport | Problem]
    """

    kwargs = _get_kwargs(
        investigation_id=investigation_id,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    format_: ExportInvestigationFormat | Unset = ExportInvestigationFormat.JSON,
) -> InvestigationExport | Problem | None:
    """Export an investigation

     JSON, Markdown report with citations `[source/externalId@vN]`, or GeoJSON. Every format carries the
    header "Automated analysis inputs; verify before acting" and licence attributions.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        investigation_id (str):
        format_ (ExportInvestigationFormat | Unset):  Default: ExportInvestigationFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationExport | Problem
    """

    return (
        await asyncio_detailed(
            investigation_id=investigation_id,
            client=client,
            format_=format_,
        )
    ).parsed
