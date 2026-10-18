// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ground_station_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroundStationPayloadCWProxy {
  GroundStationPayload state(GroundStationPayloadStateEnum state);

  GroundStationPayload currentTarget(String? currentTarget);

  GroundStationPayload frequencyHz(int? frequencyHz);

  GroundStationPayload az(int? az);

  GroundStationPayload el(int? el);

  GroundStationPayload snrDb(int? snrDb);

  GroundStationPayload nextAos(DateTime? nextAos);

  GroundStationPayload nextLos(DateTime? nextLos);

  GroundStationPayload rotatorEngaged(bool? rotatorEngaged);

  GroundStationPayload satdumpMode(
    GroundStationPayloadSatdumpModeEnum? satdumpMode,
  );

  GroundStationPayload title(String title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroundStationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroundStationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  GroundStationPayload call({
    GroundStationPayloadStateEnum state,
    String? currentTarget,
    int? frequencyHz,
    int? az,
    int? el,
    int? snrDb,
    DateTime? nextAos,
    DateTime? nextLos,
    bool? rotatorEngaged,
    GroundStationPayloadSatdumpModeEnum? satdumpMode,
    String title,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroundStationPayload.copyWith(...)` or call `instanceOfGroundStationPayload.copyWith.fieldName(value)` for a single field.
class _$GroundStationPayloadCWProxyImpl
    implements _$GroundStationPayloadCWProxy {
  const _$GroundStationPayloadCWProxyImpl(this._value);

  final GroundStationPayload _value;

  @override
  GroundStationPayload state(GroundStationPayloadStateEnum state) =>
      call(state: state);

  @override
  GroundStationPayload currentTarget(String? currentTarget) =>
      call(currentTarget: currentTarget);

  @override
  GroundStationPayload frequencyHz(int? frequencyHz) =>
      call(frequencyHz: frequencyHz);

  @override
  GroundStationPayload az(int? az) => call(az: az);

  @override
  GroundStationPayload el(int? el) => call(el: el);

  @override
  GroundStationPayload snrDb(int? snrDb) => call(snrDb: snrDb);

  @override
  GroundStationPayload nextAos(DateTime? nextAos) => call(nextAos: nextAos);

  @override
  GroundStationPayload nextLos(DateTime? nextLos) => call(nextLos: nextLos);

  @override
  GroundStationPayload rotatorEngaged(bool? rotatorEngaged) =>
      call(rotatorEngaged: rotatorEngaged);

  @override
  GroundStationPayload satdumpMode(
    GroundStationPayloadSatdumpModeEnum? satdumpMode,
  ) => call(satdumpMode: satdumpMode);

  @override
  GroundStationPayload title(String title) => call(title: title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroundStationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroundStationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroundStationPayload call({
    Object? state = const $CopyWithPlaceholder(),
    Object? currentTarget = const $CopyWithPlaceholder(),
    Object? frequencyHz = const $CopyWithPlaceholder(),
    Object? az = const $CopyWithPlaceholder(),
    Object? el = const $CopyWithPlaceholder(),
    Object? snrDb = const $CopyWithPlaceholder(),
    Object? nextAos = const $CopyWithPlaceholder(),
    Object? nextLos = const $CopyWithPlaceholder(),
    Object? rotatorEngaged = const $CopyWithPlaceholder(),
    Object? satdumpMode = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
  }) {
    return GroundStationPayload(
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as GroundStationPayloadStateEnum,
      currentTarget: currentTarget == const $CopyWithPlaceholder()
          ? _value.currentTarget
          // ignore: cast_nullable_to_non_nullable
          : currentTarget as String?,
      frequencyHz: frequencyHz == const $CopyWithPlaceholder()
          ? _value.frequencyHz
          // ignore: cast_nullable_to_non_nullable
          : frequencyHz as int?,
      az: az == const $CopyWithPlaceholder()
          ? _value.az
          // ignore: cast_nullable_to_non_nullable
          : az as int?,
      el: el == const $CopyWithPlaceholder()
          ? _value.el
          // ignore: cast_nullable_to_non_nullable
          : el as int?,
      snrDb: snrDb == const $CopyWithPlaceholder()
          ? _value.snrDb
          // ignore: cast_nullable_to_non_nullable
          : snrDb as int?,
      nextAos: nextAos == const $CopyWithPlaceholder()
          ? _value.nextAos
          // ignore: cast_nullable_to_non_nullable
          : nextAos as DateTime?,
      nextLos: nextLos == const $CopyWithPlaceholder()
          ? _value.nextLos
          // ignore: cast_nullable_to_non_nullable
          : nextLos as DateTime?,
      rotatorEngaged: rotatorEngaged == const $CopyWithPlaceholder()
          ? _value.rotatorEngaged
          // ignore: cast_nullable_to_non_nullable
          : rotatorEngaged as bool?,
      satdumpMode: satdumpMode == const $CopyWithPlaceholder()
          ? _value.satdumpMode
          // ignore: cast_nullable_to_non_nullable
          : satdumpMode as GroundStationPayloadSatdumpModeEnum?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
    );
  }
}

extension $GroundStationPayloadCopyWith on GroundStationPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroundStationPayload.copyWith(...)` or `instanceOfGroundStationPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroundStationPayloadCWProxy get copyWith =>
      _$GroundStationPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroundStationPayload _$GroundStationPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GroundStationPayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['state', 'title']);
  final val = GroundStationPayload(
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$GroundStationPayloadStateEnumEnumMap, v),
    ),
    currentTarget: $checkedConvert('currentTarget', (v) => v as String?),
    frequencyHz: $checkedConvert('frequencyHz', (v) => (v as num?)?.toInt()),
    az: $checkedConvert('az', (v) => (v as num?)?.toInt()),
    el: $checkedConvert('el', (v) => (v as num?)?.toInt()),
    snrDb: $checkedConvert('snrDb', (v) => (v as num?)?.toInt()),
    nextAos: $checkedConvert(
      'nextAos',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    nextLos: $checkedConvert(
      'nextLos',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    rotatorEngaged: $checkedConvert('rotatorEngaged', (v) => v as bool?),
    satdumpMode: $checkedConvert(
      'satdumpMode',
      (v) =>
          $enumDecodeNullable(_$GroundStationPayloadSatdumpModeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$GroundStationPayloadToJson(
  GroundStationPayload instance,
) => <String, dynamic>{
  'state': _$GroundStationPayloadStateEnumEnumMap[instance.state]!,
  'currentTarget': ?instance.currentTarget,
  'frequencyHz': ?instance.frequencyHz,
  'az': ?instance.az,
  'el': ?instance.el,
  'snrDb': ?instance.snrDb,
  'nextAos': ?instance.nextAos?.toIso8601String(),
  'nextLos': ?instance.nextLos?.toIso8601String(),
  'rotatorEngaged': ?instance.rotatorEngaged,
  'satdumpMode':
      ?_$GroundStationPayloadSatdumpModeEnumEnumMap[instance.satdumpMode],
  'title': instance.title,
};

const _$GroundStationPayloadStateEnumEnumMap = {
  GroundStationPayloadStateEnum.idle: 'idle',
  GroundStationPayloadStateEnum.tracking: 'tracking',
  GroundStationPayloadStateEnum.receiving: 'receiving',
  GroundStationPayloadStateEnum.locked: 'locked',
  GroundStationPayloadStateEnum.unknown: 'unknown',
};

const _$GroundStationPayloadSatdumpModeEnumEnumMap = {
  GroundStationPayloadSatdumpModeEnum.live: 'live',
  GroundStationPayloadSatdumpModeEnum.autotrack: 'autotrack',
};
