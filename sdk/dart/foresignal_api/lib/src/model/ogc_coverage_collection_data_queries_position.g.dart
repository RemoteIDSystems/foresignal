// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_data_queries_position.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionDataQueriesPositionCWProxy {
  OgcCoverageCollectionDataQueriesPosition link(OgcLink? link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesPosition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesPosition(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionDataQueriesPosition call({OgcLink? link});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionDataQueriesPosition.copyWith(...)` or call `instanceOfOgcCoverageCollectionDataQueriesPosition.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionDataQueriesPositionCWProxyImpl
    implements _$OgcCoverageCollectionDataQueriesPositionCWProxy {
  const _$OgcCoverageCollectionDataQueriesPositionCWProxyImpl(this._value);

  final OgcCoverageCollectionDataQueriesPosition _value;

  @override
  OgcCoverageCollectionDataQueriesPosition link(OgcLink? link) =>
      call(link: link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesPosition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesPosition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionDataQueriesPosition call({
    Object? link = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionDataQueriesPosition(
      link: link == const $CopyWithPlaceholder()
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as OgcLink?,
    );
  }
}

extension $OgcCoverageCollectionDataQueriesPositionCopyWith
    on OgcCoverageCollectionDataQueriesPosition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionDataQueriesPosition.copyWith(...)` or `instanceOfOgcCoverageCollectionDataQueriesPosition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionDataQueriesPositionCWProxy get copyWith =>
      _$OgcCoverageCollectionDataQueriesPositionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionDataQueriesPosition
_$OgcCoverageCollectionDataQueriesPositionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCoverageCollectionDataQueriesPosition', json, (
      $checkedConvert,
    ) {
      final val = OgcCoverageCollectionDataQueriesPosition(
        link: $checkedConvert(
          'link',
          (v) => v == null ? null : OgcLink.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCoverageCollectionDataQueriesPositionToJson(
  OgcCoverageCollectionDataQueriesPosition instance,
) => <String, dynamic>{'link': ?instance.link?.toJson()};
