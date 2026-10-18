// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'jamming_cell_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JammingCellPayloadCWProxy {
  JammingCellPayload automated(bool automated);

  JammingCellPayload notAWarning(bool notAWarning);

  JammingCellPayload detector(DerivedPayloadCommonDetector detector);

  JammingCellPayload confidence(num? confidence);

  JammingCellPayload status(JammingCellPayloadStatusEnum status);

  JammingCellPayload evidence(List<DerivedEvidenceRef>? evidence);

  JammingCellPayload level(JammingCellPayloadLevelEnum level);

  JammingCellPayload grid(JammingCellPayloadGridEnum? grid);

  JammingCellPayload cell(String cell);

  JammingCellPayload aircraft(int? aircraft);

  JammingCellPayload badAircraft(int? badAircraft);

  JammingCellPayload ratio(num? ratio);

  JammingCellPayload windowMinutes(int? windowMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JammingCellPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JammingCellPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  JammingCellPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    JammingCellPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    JammingCellPayloadLevelEnum level,
    JammingCellPayloadGridEnum? grid,
    String cell,
    int? aircraft,
    int? badAircraft,
    num? ratio,
    int? windowMinutes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJammingCellPayload.copyWith(...)` or call `instanceOfJammingCellPayload.copyWith.fieldName(value)` for a single field.
class _$JammingCellPayloadCWProxyImpl implements _$JammingCellPayloadCWProxy {
  const _$JammingCellPayloadCWProxyImpl(this._value);

  final JammingCellPayload _value;

  @override
  JammingCellPayload automated(bool automated) => call(automated: automated);

  @override
  JammingCellPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  JammingCellPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  JammingCellPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  JammingCellPayload status(JammingCellPayloadStatusEnum status) =>
      call(status: status);

  @override
  JammingCellPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  JammingCellPayload level(JammingCellPayloadLevelEnum level) =>
      call(level: level);

  @override
  JammingCellPayload grid(JammingCellPayloadGridEnum? grid) => call(grid: grid);

  @override
  JammingCellPayload cell(String cell) => call(cell: cell);

  @override
  JammingCellPayload aircraft(int? aircraft) => call(aircraft: aircraft);

  @override
  JammingCellPayload badAircraft(int? badAircraft) =>
      call(badAircraft: badAircraft);

  @override
  JammingCellPayload ratio(num? ratio) => call(ratio: ratio);

  @override
  JammingCellPayload windowMinutes(int? windowMinutes) =>
      call(windowMinutes: windowMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JammingCellPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JammingCellPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JammingCellPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? level = const $CopyWithPlaceholder(),
    Object? grid = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? aircraft = const $CopyWithPlaceholder(),
    Object? badAircraft = const $CopyWithPlaceholder(),
    Object? ratio = const $CopyWithPlaceholder(),
    Object? windowMinutes = const $CopyWithPlaceholder(),
  }) {
    return JammingCellPayload(
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
      notAWarning:
          notAWarning == const $CopyWithPlaceholder() || notAWarning == null
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DerivedPayloadCommonDetector,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as JammingCellPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      level: level == const $CopyWithPlaceholder() || level == null
          ? _value.level
          // ignore: cast_nullable_to_non_nullable
          : level as JammingCellPayloadLevelEnum,
      grid: grid == const $CopyWithPlaceholder()
          ? _value.grid
          // ignore: cast_nullable_to_non_nullable
          : grid as JammingCellPayloadGridEnum?,
      cell: cell == const $CopyWithPlaceholder() || cell == null
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String,
      aircraft: aircraft == const $CopyWithPlaceholder()
          ? _value.aircraft
          // ignore: cast_nullable_to_non_nullable
          : aircraft as int?,
      badAircraft: badAircraft == const $CopyWithPlaceholder()
          ? _value.badAircraft
          // ignore: cast_nullable_to_non_nullable
          : badAircraft as int?,
      ratio: ratio == const $CopyWithPlaceholder()
          ? _value.ratio
          // ignore: cast_nullable_to_non_nullable
          : ratio as num?,
      windowMinutes: windowMinutes == const $CopyWithPlaceholder()
          ? _value.windowMinutes
          // ignore: cast_nullable_to_non_nullable
          : windowMinutes as int?,
    );
  }
}

extension $JammingCellPayloadCopyWith on JammingCellPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJammingCellPayload.copyWith(...)` or `instanceOfJammingCellPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JammingCellPayloadCWProxy get copyWith =>
      _$JammingCellPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JammingCellPayload _$JammingCellPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('JammingCellPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'level',
      'cell',
    ],
  );
  final val = JammingCellPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$JammingCellPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    level: $checkedConvert(
      'level',
      (v) => $enumDecode(_$JammingCellPayloadLevelEnumEnumMap, v),
    ),
    grid: $checkedConvert(
      'grid',
      (v) => $enumDecodeNullable(_$JammingCellPayloadGridEnumEnumMap, v),
    ),
    cell: $checkedConvert('cell', (v) => v as String),
    aircraft: $checkedConvert('aircraft', (v) => (v as num?)?.toInt()),
    badAircraft: $checkedConvert('badAircraft', (v) => (v as num?)?.toInt()),
    ratio: $checkedConvert('ratio', (v) => v as num?),
    windowMinutes: $checkedConvert(
      'windowMinutes',
      (v) => (v as num?)?.toInt() ?? 15,
    ),
  );
  return val;
});

Map<String, dynamic> _$JammingCellPayloadToJson(JammingCellPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$JammingCellPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'level': _$JammingCellPayloadLevelEnumEnumMap[instance.level]!,
      'grid': ?_$JammingCellPayloadGridEnumEnumMap[instance.grid],
      'cell': instance.cell,
      'aircraft': ?instance.aircraft,
      'badAircraft': ?instance.badAircraft,
      'ratio': ?instance.ratio,
      'windowMinutes': ?instance.windowMinutes,
    };

const _$JammingCellPayloadStatusEnumEnumMap = {
  JammingCellPayloadStatusEnum.ok: 'ok',
  JammingCellPayloadStatusEnum.insufficientData: 'insufficient-data',
  JammingCellPayloadStatusEnum.noCoverage: 'no-coverage',
  JammingCellPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$JammingCellPayloadLevelEnumEnumMap = {
  JammingCellPayloadLevelEnum.medium: 'medium',
  JammingCellPayloadLevelEnum.high: 'high',
};

const _$JammingCellPayloadGridEnumEnumMap = {
  JammingCellPayloadGridEnum.h3: 'h3',
  JammingCellPayloadGridEnum.deg1: 'deg1',
};
