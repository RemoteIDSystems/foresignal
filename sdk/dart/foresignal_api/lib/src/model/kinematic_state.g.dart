// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kinematic_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KinematicStateCWProxy {
  KinematicState sourceId(String sourceId);

  KinematicState externalId(String externalId);

  KinematicState kind(String? kind);

  KinematicState position(Geometry position);

  KinematicState observedAt(DateTime observedAt);

  KinematicState speedMps(num? speedMps);

  KinematicState courseDeg(num? courseDeg);

  KinematicState altitudeM(num? altitudeM);

  KinematicState derived(bool? derived);

  KinematicState stale(bool? stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KinematicState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KinematicState(...).copyWith(id: 12, name: "My name")
  /// ```
  KinematicState call({
    String sourceId,
    String externalId,
    String? kind,
    Geometry position,
    DateTime observedAt,
    num? speedMps,
    num? courseDeg,
    num? altitudeM,
    bool? derived,
    bool? stale,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKinematicState.copyWith(...)` or call `instanceOfKinematicState.copyWith.fieldName(value)` for a single field.
class _$KinematicStateCWProxyImpl implements _$KinematicStateCWProxy {
  const _$KinematicStateCWProxyImpl(this._value);

  final KinematicState _value;

  @override
  KinematicState sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  KinematicState externalId(String externalId) => call(externalId: externalId);

  @override
  KinematicState kind(String? kind) => call(kind: kind);

  @override
  KinematicState position(Geometry position) => call(position: position);

  @override
  KinematicState observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  KinematicState speedMps(num? speedMps) => call(speedMps: speedMps);

  @override
  KinematicState courseDeg(num? courseDeg) => call(courseDeg: courseDeg);

  @override
  KinematicState altitudeM(num? altitudeM) => call(altitudeM: altitudeM);

  @override
  KinematicState derived(bool? derived) => call(derived: derived);

  @override
  KinematicState stale(bool? stale) => call(stale: stale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KinematicState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KinematicState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KinematicState call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? position = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? speedMps = const $CopyWithPlaceholder(),
    Object? courseDeg = const $CopyWithPlaceholder(),
    Object? altitudeM = const $CopyWithPlaceholder(),
    Object? derived = const $CopyWithPlaceholder(),
    Object? stale = const $CopyWithPlaceholder(),
  }) {
    return KinematicState(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      position: position == const $CopyWithPlaceholder() || position == null
          ? _value.position
          // ignore: cast_nullable_to_non_nullable
          : position as Geometry,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      speedMps: speedMps == const $CopyWithPlaceholder()
          ? _value.speedMps
          // ignore: cast_nullable_to_non_nullable
          : speedMps as num?,
      courseDeg: courseDeg == const $CopyWithPlaceholder()
          ? _value.courseDeg
          // ignore: cast_nullable_to_non_nullable
          : courseDeg as num?,
      altitudeM: altitudeM == const $CopyWithPlaceholder()
          ? _value.altitudeM
          // ignore: cast_nullable_to_non_nullable
          : altitudeM as num?,
      derived: derived == const $CopyWithPlaceholder()
          ? _value.derived
          // ignore: cast_nullable_to_non_nullable
          : derived as bool?,
      stale: stale == const $CopyWithPlaceholder()
          ? _value.stale
          // ignore: cast_nullable_to_non_nullable
          : stale as bool?,
    );
  }
}

extension $KinematicStateCopyWith on KinematicState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKinematicState.copyWith(...)` or `instanceOfKinematicState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KinematicStateCWProxy get copyWith => _$KinematicStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KinematicState _$KinematicStateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KinematicState', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'externalId',
          'position',
          'observedAt',
        ],
      );
      final val = KinematicState(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String?),
        position: $checkedConvert(
          'position',
          (v) => Geometry.fromJson(v as Map<String, dynamic>),
        ),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        speedMps: $checkedConvert('speedMps', (v) => v as num?),
        courseDeg: $checkedConvert('courseDeg', (v) => v as num?),
        altitudeM: $checkedConvert('altitudeM', (v) => v as num?),
        derived: $checkedConvert('derived', (v) => v as bool?),
        stale: $checkedConvert('stale', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$KinematicStateToJson(KinematicState instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'kind': ?instance.kind,
      'position': instance.position.toJson(),
      'observedAt': instance.observedAt.toIso8601String(),
      'speedMps': ?instance.speedMps,
      'courseDeg': ?instance.courseDeg,
      'altitudeM': ?instance.altitudeM,
      'derived': ?instance.derived,
      'stale': ?instance.stale,
    };
