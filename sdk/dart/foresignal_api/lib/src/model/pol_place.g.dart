// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_place.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolPlaceCWProxy {
  PolPlace id(String id);

  PolPlace subjectId(String subjectId);

  PolPlace clusterId(int clusterId);

  PolPlace centroid(Geometry centroid);

  PolPlace radiusM(num radiusM);

  PolPlace visits(int visits);

  PolPlace totalDwellMinutes(num totalDwellMinutes);

  PolPlace hourOfWeek(List<num> hourOfWeek);

  PolPlace firstSeen(DateTime firstSeen);

  PolPlace lastSeen(DateTime lastSeen);

  PolPlace label(String? label);

  PolPlace private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlace(...).copyWith(id: 12, name: "My name")
  /// ```
  PolPlace call({
    String id,
    String subjectId,
    int clusterId,
    Geometry centroid,
    num radiusM,
    int visits,
    num totalDwellMinutes,
    List<num> hourOfWeek,
    DateTime firstSeen,
    DateTime lastSeen,
    String? label,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolPlace.copyWith(...)` or call `instanceOfPolPlace.copyWith.fieldName(value)` for a single field.
class _$PolPlaceCWProxyImpl implements _$PolPlaceCWProxy {
  const _$PolPlaceCWProxyImpl(this._value);

  final PolPlace _value;

  @override
  PolPlace id(String id) => call(id: id);

  @override
  PolPlace subjectId(String subjectId) => call(subjectId: subjectId);

  @override
  PolPlace clusterId(int clusterId) => call(clusterId: clusterId);

  @override
  PolPlace centroid(Geometry centroid) => call(centroid: centroid);

  @override
  PolPlace radiusM(num radiusM) => call(radiusM: radiusM);

  @override
  PolPlace visits(int visits) => call(visits: visits);

  @override
  PolPlace totalDwellMinutes(num totalDwellMinutes) =>
      call(totalDwellMinutes: totalDwellMinutes);

  @override
  PolPlace hourOfWeek(List<num> hourOfWeek) => call(hourOfWeek: hourOfWeek);

  @override
  PolPlace firstSeen(DateTime firstSeen) => call(firstSeen: firstSeen);

  @override
  PolPlace lastSeen(DateTime lastSeen) => call(lastSeen: lastSeen);

  @override
  PolPlace label(String? label) => call(label: label);

  @override
  PolPlace private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlace(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolPlace call({
    Object? id = const $CopyWithPlaceholder(),
    Object? subjectId = const $CopyWithPlaceholder(),
    Object? clusterId = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
    Object? radiusM = const $CopyWithPlaceholder(),
    Object? visits = const $CopyWithPlaceholder(),
    Object? totalDwellMinutes = const $CopyWithPlaceholder(),
    Object? hourOfWeek = const $CopyWithPlaceholder(),
    Object? firstSeen = const $CopyWithPlaceholder(),
    Object? lastSeen = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return PolPlace(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      subjectId: subjectId == const $CopyWithPlaceholder() || subjectId == null
          ? _value.subjectId
          // ignore: cast_nullable_to_non_nullable
          : subjectId as String,
      clusterId: clusterId == const $CopyWithPlaceholder() || clusterId == null
          ? _value.clusterId
          // ignore: cast_nullable_to_non_nullable
          : clusterId as int,
      centroid: centroid == const $CopyWithPlaceholder() || centroid == null
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry,
      radiusM: radiusM == const $CopyWithPlaceholder() || radiusM == null
          ? _value.radiusM
          // ignore: cast_nullable_to_non_nullable
          : radiusM as num,
      visits: visits == const $CopyWithPlaceholder() || visits == null
          ? _value.visits
          // ignore: cast_nullable_to_non_nullable
          : visits as int,
      totalDwellMinutes:
          totalDwellMinutes == const $CopyWithPlaceholder() ||
              totalDwellMinutes == null
          ? _value.totalDwellMinutes
          // ignore: cast_nullable_to_non_nullable
          : totalDwellMinutes as num,
      hourOfWeek:
          hourOfWeek == const $CopyWithPlaceholder() || hourOfWeek == null
          ? _value.hourOfWeek
          // ignore: cast_nullable_to_non_nullable
          : hourOfWeek as List<num>,
      firstSeen: firstSeen == const $CopyWithPlaceholder() || firstSeen == null
          ? _value.firstSeen
          // ignore: cast_nullable_to_non_nullable
          : firstSeen as DateTime,
      lastSeen: lastSeen == const $CopyWithPlaceholder() || lastSeen == null
          ? _value.lastSeen
          // ignore: cast_nullable_to_non_nullable
          : lastSeen as DateTime,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $PolPlaceCopyWith on PolPlace {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolPlace.copyWith(...)` or `instanceOfPolPlace.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolPlaceCWProxy get copyWith => _$PolPlaceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolPlace _$PolPlaceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PolPlace', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'subjectId',
      'clusterId',
      'centroid',
      'radiusM',
      'visits',
      'totalDwellMinutes',
      'hourOfWeek',
      'firstSeen',
      'lastSeen',
    ],
  );
  final val = PolPlace(
    id: $checkedConvert('id', (v) => v as String),
    subjectId: $checkedConvert('subjectId', (v) => v as String),
    clusterId: $checkedConvert('clusterId', (v) => (v as num).toInt()),
    centroid: $checkedConvert(
      'centroid',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    radiusM: $checkedConvert('radiusM', (v) => v as num),
    visits: $checkedConvert('visits', (v) => (v as num).toInt()),
    totalDwellMinutes: $checkedConvert('totalDwellMinutes', (v) => v as num),
    hourOfWeek: $checkedConvert(
      'hourOfWeek',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
    firstSeen: $checkedConvert('firstSeen', (v) => DateTime.parse(v as String)),
    lastSeen: $checkedConvert('lastSeen', (v) => DateTime.parse(v as String)),
    label: $checkedConvert('label', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$PolPlaceToJson(PolPlace instance) => <String, dynamic>{
  'id': instance.id,
  'subjectId': instance.subjectId,
  'clusterId': instance.clusterId,
  'centroid': instance.centroid.toJson(),
  'radiusM': instance.radiusM,
  'visits': instance.visits,
  'totalDwellMinutes': instance.totalDwellMinutes,
  'hourOfWeek': instance.hourOfWeek,
  'firstSeen': instance.firstSeen.toIso8601String(),
  'lastSeen': instance.lastSeen.toIso8601String(),
  'label': ?instance.label,
  'private': ?instance.private,
};
