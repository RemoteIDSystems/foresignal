from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.report_share_link_input import ReportShareLinkInput
from ...models.share_link import ShareLink
from ...types import UNSET, Response, Unset


def _get_kwargs(
    report_id: str,
    *,
    body: ReportShareLinkInput | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/reports/{report_id}/share-link".format(
            report_id=quote(str(report_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ShareLink | None:
    if response.status_code == 201:
        response_201 = ShareLink.from_dict(response.json())

        return response_201

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
) -> Response[Problem | ShareLink]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportShareLinkInput | Unset = UNSET,
) -> Response[Problem | ShareLink]:
    """Create a read-only report link

     Issues a 112 share key (`fss_`, shown once) serving the published HTML rendition at
    `/v1/k/{key}/report` (owner). Only TLP CLEAR, or GREEN when `exchange.allowGreenLinks=true`.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportShareLinkInput | Unset): Create a read-only link to the published HTML
            rendition (TLP CLEAR, or GREEN with `exchange.allowGreenLinks`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLink]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportShareLinkInput | Unset = UNSET,
) -> Problem | ShareLink | None:
    """Create a read-only report link

     Issues a 112 share key (`fss_`, shown once) serving the published HTML rendition at
    `/v1/k/{key}/report` (owner). Only TLP CLEAR, or GREEN when `exchange.allowGreenLinks=true`.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportShareLinkInput | Unset): Create a read-only link to the published HTML
            rendition (TLP CLEAR, or GREEN with `exchange.allowGreenLinks`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLink
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportShareLinkInput | Unset = UNSET,
) -> Response[Problem | ShareLink]:
    """Create a read-only report link

     Issues a 112 share key (`fss_`, shown once) serving the published HTML rendition at
    `/v1/k/{key}/report` (owner). Only TLP CLEAR, or GREEN when `exchange.allowGreenLinks=true`.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportShareLinkInput | Unset): Create a read-only link to the published HTML
            rendition (TLP CLEAR, or GREEN with `exchange.allowGreenLinks`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLink]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportShareLinkInput | Unset = UNSET,
) -> Problem | ShareLink | None:
    """Create a read-only report link

     Issues a 112 share key (`fss_`, shown once) serving the published HTML rendition at
    `/v1/k/{key}/report` (owner). Only TLP CLEAR, or GREEN when `exchange.allowGreenLinks=true`.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportShareLinkInput | Unset): Create a read-only link to the published HTML
            rendition (TLP CLEAR, or GREEN with `exchange.allowGreenLinks`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLink
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
            body=body,
        )
    ).parsed
