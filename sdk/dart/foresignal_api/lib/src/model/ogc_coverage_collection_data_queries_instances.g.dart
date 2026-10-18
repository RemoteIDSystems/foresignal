// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_data_queries_instances.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionDataQueriesInstancesCWProxy {
  OgcCoverageCollectionDataQueriesInstances link(OgcLink? link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesInstances(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesInstances(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionDataQueriesInstances call({OgcLink? link});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionDataQueriesInstances.copyWith(...)` or call `instanceOfOgcCoverageCollectionDataQueriesInstances.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionDataQueriesInstancesCWProxyImpl
    implements _$OgcCoverageCollectionDataQueriesInstancesCWProxy {
  const _$OgcCoverageCollectionDataQueriesInstancesCWProxyImpl(this._value);

  final OgcCoverageCollectionDataQueriesInstances _value;

  @override
  OgcCoverageCollectionDataQueriesInstances link(OgcLink? link) =>
      call(link: link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueriesInstances(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueriesInstances(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionDataQueriesInstances call({
    Object? link = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionDataQueriesInstances(
      link: link == const $CopyWithPlaceholder()
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as OgcLink?,
    );
  }
}

extension $OgcCoverageCollectionDataQueriesInstancesCopyWith
    on OgcCoverageCollectionDataQueriesInstances {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionDataQueriesInstances.copyWith(...)` or `instanceOfOgcCoverageCollectionDataQueriesInstances.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionDataQueriesInstancesCWProxy get copyWith =>
      _$OgcCoverageCollectionDataQueriesInstancesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionDataQueriesInstances
_$OgcCoverageCollectionDataQueriesInstancesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcCoverageCollectionDataQueriesInstances', json, (
  $checkedConvert,
) {
  final val = OgcCoverageCollectionDataQueriesInstances(
    link: $checkedConvert(
      'link',
      (v) => v == null ? null : OgcLink.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$OgcCoverageCollectionDataQueriesInstancesToJson(
  OgcCoverageCollectionDataQueriesInstances instance,
) => <String, dynamic>{'link': ?instance.link?.toJson()};
