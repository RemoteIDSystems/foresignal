// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_data_queries_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionDataQueriesAreaCWProxy {
  OgcCoverageCollectionDataQueriesArea link(OgcLink? link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesArea(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionDataQueriesArea call({OgcLink? link});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionDataQueriesArea.copyWith(...)` or call `instanceOfOgcCoverageCollectionDataQueriesArea.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionDataQueriesAreaCWProxyImpl
    implements _$OgcCoverageCollectionDataQueriesAreaCWProxy {
  const _$OgcCoverageCollectionDataQueriesAreaCWProxyImpl(this._value);

  final OgcCoverageCollectionDataQueriesArea _value;

  @override
  OgcCoverageCollectionDataQueriesArea link(OgcLink? link) => call(link: link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionDataQueriesArea call({
    Object? link = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionDataQueriesArea(
      link: link == const $CopyWithPlaceholder()
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as OgcLink?,
    );
  }
}

extension $OgcCoverageCollectionDataQueriesAreaCopyWith
    on OgcCoverageCollectionDataQueriesArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionDataQueriesArea.copyWith(...)` or `instanceOfOgcCoverageCollectionDataQueriesArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionDataQueriesAreaCWProxy get copyWith =>
      _$OgcCoverageCollectionDataQueriesAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionDataQueriesArea
_$OgcCoverageCollectionDataQueriesAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCoverageCollectionDataQueriesArea', json, (
      $checkedConvert,
    ) {
      final val = OgcCoverageCollectionDataQueriesArea(
        link: $checkedConvert(
          'link',
          (v) => v == null ? null : OgcLink.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCoverageCollectionDataQueriesAreaToJson(
  OgcCoverageCollectionDataQueriesArea instance,
) => <String, dynamic>{'link': ?instance.link?.toJson()};
