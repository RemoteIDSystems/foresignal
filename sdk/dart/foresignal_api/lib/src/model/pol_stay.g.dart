// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_stay.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolStayCWProxy {
  PolStay id(String id);

  PolStay subjectId(String subjectId);

  PolStay arrive(DateTime arrive);

  PolStay depart(DateTime depart);

  PolStay centroid(Geometry centroid);

  PolStay placeId(String? placeId);

  PolStay clusterId(int? clusterId);

  PolStay fixes(int? fixes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolStay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolStay(...).copyWith(id: 12, name: "My name")
  /// ```
  PolStay call({
    String id,
    String subjectId,
    DateTime arrive,
    DateTime depart,
    Geometry centroid,
    String? placeId,
    int? clusterId,
    int? fixes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolStay.copyWith(...)` or call `instanceOfPolStay.copyWith.fieldName(value)` for a single field.
class _$PolStayCWProxyImpl implements _$PolStayCWProxy {
  const _$PolStayCWProxyImpl(this._value);

  final PolStay _value;

  @override
  PolStay id(String id) => call(id: id);

  @override
  PolStay subjectId(String subjectId) => call(subjectId: subjectId);

  @override
  PolStay arrive(DateTime arrive) => call(arrive: arrive);

  @override
  PolStay depart(DateTime depart) => call(depart: depart);

  @override
  PolStay centroid(Geometry centroid) => call(centroid: centroid);

  @override
  PolStay placeId(String? placeId) => call(placeId: placeId);

  @override
  PolStay clusterId(int? clusterId) => call(clusterId: clusterId);

  @override
  PolStay fixes(int? fixes) => call(fixes: fixes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolStay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolStay(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolStay call({
    Object? id = const $CopyWithPlaceholder(),
    Object? subjectId = const $CopyWithPlaceholder(),
    Object? arrive = const $CopyWithPlaceholder(),
    Object? depart = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? clusterId = const $CopyWithPlaceholder(),
    Object? fixes = const $CopyWithPlaceholder(),
  }) {
    return PolStay(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      subjectId: subjectId == const $CopyWithPlaceholder() || subjectId == null
          ? _value.subjectId
          // ignore: cast_nullable_to_non_nullable
          : subjectId as String,
      arrive: arrive == const $CopyWithPlaceholder() || arrive == null
          ? _value.arrive
          // ignore: cast_nullable_to_non_nullable
          : arrive as DateTime,
      depart: depart == const $CopyWithPlaceholder() || depart == null
          ? _value.depart
          // ignore: cast_nullable_to_non_nullable
          : depart as DateTime,
      centroid: centroid == const $CopyWithPlaceholder() || centroid == null
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      clusterId: clusterId == const $CopyWithPlaceholder()
          ? _value.clusterId
          // ignore: cast_nullable_to_non_nullable
          : clusterId as int?,
      fixes: fixes == const $CopyWithPlaceholder()
          ? _value.fixes
          // ignore: cast_nullable_to_non_nullable
          : fixes as int?,
    );
  }
}

extension $PolStayCopyWith on PolStay {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolStay.copyWith(...)` or `instanceOfPolStay.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolStayCWProxy get copyWith => _$PolStayCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolStay _$PolStayFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolStay', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'subjectId', 'arrive', 'depart', 'centroid'],
      );
      final val = PolStay(
        id: $checkedConvert('id', (v) => v as String),
        subjectId: $checkedConvert('subjectId', (v) => v as String),
        arrive: $checkedConvert('arrive', (v) => DateTime.parse(v as String)),
        depart: $checkedConvert('depart', (v) => DateTime.parse(v as String)),
        centroid: $checkedConvert(
          'centroid',
          (v) => Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        clusterId: $checkedConvert('clusterId', (v) => (v as num?)?.toInt()),
        fixes: $checkedConvert('fixes', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$PolStayToJson(PolStay instance) => <String, dynamic>{
  'id': instance.id,
  'subjectId': instance.subjectId,
  'arrive': instance.arrive.toIso8601String(),
  'depart': instance.depart.toIso8601String(),
  'centroid': instance.centroid.toJson(),
  'placeId': ?instance.placeId,
  'clusterId': ?instance.clusterId,
  'fixes': ?instance.fixes,
};
