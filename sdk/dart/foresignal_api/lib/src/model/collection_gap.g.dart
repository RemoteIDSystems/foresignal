// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_gap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionGapCWProxy {
  CollectionGap id(String id);

  CollectionGap setId(String setId);

  CollectionGap indicatorId(String indicatorId);

  CollectionGap potentialEig(num potentialEig);

  CollectionGap actualEig(num actualEig);

  CollectionGap pDetect(num? pDetect);

  CollectionGap recommendations(
    List<CollectionGapRecommendationsInner> recommendations,
  );

  CollectionGap createdAt(DateTime? createdAt);

  CollectionGap updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionGap call({
    String id,
    String setId,
    String indicatorId,
    num potentialEig,
    num actualEig,
    num? pDetect,
    List<CollectionGapRecommendationsInner> recommendations,
    DateTime? createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionGap.copyWith(...)` or call `instanceOfCollectionGap.copyWith.fieldName(value)` for a single field.
class _$CollectionGapCWProxyImpl implements _$CollectionGapCWProxy {
  const _$CollectionGapCWProxyImpl(this._value);

  final CollectionGap _value;

  @override
  CollectionGap id(String id) => call(id: id);

  @override
  CollectionGap setId(String setId) => call(setId: setId);

  @override
  CollectionGap indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  CollectionGap potentialEig(num potentialEig) =>
      call(potentialEig: potentialEig);

  @override
  CollectionGap actualEig(num actualEig) => call(actualEig: actualEig);

  @override
  CollectionGap pDetect(num? pDetect) => call(pDetect: pDetect);

  @override
  CollectionGap recommendations(
    List<CollectionGapRecommendationsInner> recommendations,
  ) => call(recommendations: recommendations);

  @override
  CollectionGap createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  CollectionGap updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionGap call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? potentialEig = const $CopyWithPlaceholder(),
    Object? actualEig = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? recommendations = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return CollectionGap(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      potentialEig:
          potentialEig == const $CopyWithPlaceholder() || potentialEig == null
          ? _value.potentialEig
          // ignore: cast_nullable_to_non_nullable
          : potentialEig as num,
      actualEig: actualEig == const $CopyWithPlaceholder() || actualEig == null
          ? _value.actualEig
          // ignore: cast_nullable_to_non_nullable
          : actualEig as num,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
      recommendations:
          recommendations == const $CopyWithPlaceholder() ||
              recommendations == null
          ? _value.recommendations
          // ignore: cast_nullable_to_non_nullable
          : recommendations as List<CollectionGapRecommendationsInner>,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $CollectionGapCopyWith on CollectionGap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionGap.copyWith(...)` or `instanceOfCollectionGap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionGapCWProxy get copyWith => _$CollectionGapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionGap _$CollectionGapFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectionGap', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'setId',
          'indicatorId',
          'potentialEig',
          'actualEig',
          'recommendations',
        ],
      );
      final val = CollectionGap(
        id: $checkedConvert('id', (v) => v as String),
        setId: $checkedConvert('setId', (v) => v as String),
        indicatorId: $checkedConvert('indicatorId', (v) => v as String),
        potentialEig: $checkedConvert('potentialEig', (v) => v as num),
        actualEig: $checkedConvert('actualEig', (v) => v as num),
        pDetect: $checkedConvert('pDetect', (v) => v as num?),
        recommendations: $checkedConvert(
          'recommendations',
          (v) => (v as List<dynamic>)
              .map(
                (e) => CollectionGapRecommendationsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollectionGapToJson(
  CollectionGap instance,
) => <String, dynamic>{
  'id': instance.id,
  'setId': instance.setId,
  'indicatorId': instance.indicatorId,
  'potentialEig': instance.potentialEig,
  'actualEig': instance.actualEig,
  'pDetect': ?instance.pDetect,
  'recommendations': instance.recommendations.map((e) => e.toJson()).toList(),
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};
