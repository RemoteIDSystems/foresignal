// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_data_queries.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionDataQueriesCWProxy {
  OgcCoverageCollectionDataQueries position(
    OgcCoverageCollectionDataQueriesPosition? position,
  );

  OgcCoverageCollectionDataQueries area(
    OgcCoverageCollectionDataQueriesArea? area,
  );

  OgcCoverageCollectionDataQueries instances(
    OgcCoverageCollectionDataQueriesInstances? instances,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueries(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionDataQueries call({
    OgcCoverageCollectionDataQueriesPosition? position,
    OgcCoverageCollectionDataQueriesArea? area,
    OgcCoverageCollectionDataQueriesInstances? instances,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionDataQueries.copyWith(...)` or call `instanceOfOgcCoverageCollectionDataQueries.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionDataQueriesCWProxyImpl
    implements _$OgcCoverageCollectionDataQueriesCWProxy {
  const _$OgcCoverageCollectionDataQueriesCWProxyImpl(this._value);

  final OgcCoverageCollectionDataQueries _value;

  @override
  OgcCoverageCollectionDataQueries position(
    OgcCoverageCollectionDataQueriesPosition? position,
  ) => call(position: position);

  @override
  OgcCoverageCollectionDataQueries area(
    OgcCoverageCollectionDataQueriesArea? area,
  ) => call(area: area);

  @override
  OgcCoverageCollectionDataQueries instances(
    OgcCoverageCollectionDataQueriesInstances? instances,
  ) => call(instances: instances);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionDataQueries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionDataQueries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionDataQueries call({
    Object? position = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? instances = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionDataQueries(
      position: position == const $CopyWithPlaceholder()
          ? _value.position
          // ignore: cast_nullable_to_non_nullable
          : position as OgcCoverageCollectionDataQueriesPosition?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as OgcCoverageCollectionDataQueriesArea?,
      instances: instances == const $CopyWithPlaceholder()
          ? _value.instances
          // ignore: cast_nullable_to_non_nullable
          : instances as OgcCoverageCollectionDataQueriesInstances?,
    );
  }
}

extension $OgcCoverageCollectionDataQueriesCopyWith
    on OgcCoverageCollectionDataQueries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionDataQueries.copyWith(...)` or `instanceOfOgcCoverageCollectionDataQueries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionDataQueriesCWProxy get copyWith =>
      _$OgcCoverageCollectionDataQueriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionDataQueries _$OgcCoverageCollectionDataQueriesFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('OgcCoverageCollectionDataQueries', json, ($checkedConvert) {
      final val = OgcCoverageCollectionDataQueries(
        position: $checkedConvert(
          'position',
          (v) => v == null
              ? null
              : OgcCoverageCollectionDataQueriesPosition.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        area: $checkedConvert(
          'area',
          (v) => v == null
              ? null
              : OgcCoverageCollectionDataQueriesArea.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        instances: $checkedConvert(
          'instances',
          (v) => v == null
              ? null
              : OgcCoverageCollectionDataQueriesInstances.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCoverageCollectionDataQueriesToJson(
  OgcCoverageCollectionDataQueries instance,
) => <String, dynamic>{
  'position': ?instance.position?.toJson(),
  'area': ?instance.area?.toJson(),
  'instances': ?instance.instances?.toJson(),
};
