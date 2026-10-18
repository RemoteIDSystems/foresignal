// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_timeline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsTimelineCWProxy {
  DossierSectionsTimeline status(DossierSectionsTimelineStatusEnum status);

  DossierSectionsTimeline message(String? message);

  DossierSectionsTimeline links(DossierSectionsOverviewLinks? links);

  DossierSectionsTimeline facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsTimeline truncated(bool? truncated);

  DossierSectionsTimeline items(List<DossierSectionsTimelineItemsInner>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsTimeline call({
    DossierSectionsTimelineStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<DossierSectionsTimelineItemsInner>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsTimeline.copyWith(...)` or call `instanceOfDossierSectionsTimeline.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsTimelineCWProxyImpl
    implements _$DossierSectionsTimelineCWProxy {
  const _$DossierSectionsTimelineCWProxyImpl(this._value);

  final DossierSectionsTimeline _value;

  @override
  DossierSectionsTimeline status(DossierSectionsTimelineStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsTimeline message(String? message) => call(message: message);

  @override
  DossierSectionsTimeline links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsTimeline facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsTimeline truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsTimeline items(
    List<DossierSectionsTimelineItemsInner>? items,
  ) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsTimeline call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsTimeline(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsTimelineStatusEnum,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as DossierSectionsOverviewLinks?,
      facets: facets == const $CopyWithPlaceholder()
          ? _value.facets
          // ignore: cast_nullable_to_non_nullable
          : facets as DossierSectionsOverviewFacets?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<DossierSectionsTimelineItemsInner>?,
    );
  }
}

extension $DossierSectionsTimelineCopyWith on DossierSectionsTimeline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsTimeline.copyWith(...)` or `instanceOfDossierSectionsTimeline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsTimelineCWProxy get copyWith =>
      _$DossierSectionsTimelineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsTimeline _$DossierSectionsTimelineFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsTimeline', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsTimeline(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsTimelineStatusEnumEnumMap, v),
    ),
    message: $checkedConvert('message', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => v == null
          ? null
          : DossierSectionsOverviewLinks.fromJson(v as Map<String, dynamic>),
    ),
    facets: $checkedConvert(
      'facets',
      (v) => v == null
          ? null
          : DossierSectionsOverviewFacets.fromJson(v as Map<String, dynamic>),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DossierSectionsTimelineItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsTimelineToJson(
  DossierSectionsTimeline instance,
) => <String, dynamic>{
  'status': _$DossierSectionsTimelineStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'items': ?instance.items?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsTimelineStatusEnumEnumMap = {
  DossierSectionsTimelineStatusEnum.ok: 'ok',
  DossierSectionsTimelineStatusEnum.empty: 'empty',
  DossierSectionsTimelineStatusEnum.unavailable: 'unavailable',
  DossierSectionsTimelineStatusEnum.notInstalled: 'not-installed',
};
