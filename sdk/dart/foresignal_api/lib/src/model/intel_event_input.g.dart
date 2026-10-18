// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventInputCWProxy {
  IntelEventInput type(IntelEventInputTypeEnum type);

  IntelEventInput title(String title);

  IntelEventInput severity(Severity? severity);

  IntelEventInput origin(IntelEventInputOriginEnum? origin);

  IntelEventInput geometry(Geometry? geometry);

  IntelEventInput startTime(DateTime? startTime);

  IntelEventInput endTime(DateTime? endTime);

  IntelEventInput notes(String? notes);

  IntelEventInput members(List<IntelEventMemberInput> members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventInput call({
    IntelEventInputTypeEnum type,
    String title,
    Severity? severity,
    IntelEventInputOriginEnum? origin,
    Geometry? geometry,
    DateTime? startTime,
    DateTime? endTime,
    String? notes,
    List<IntelEventMemberInput> members,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventInput.copyWith(...)` or call `instanceOfIntelEventInput.copyWith.fieldName(value)` for a single field.
class _$IntelEventInputCWProxyImpl implements _$IntelEventInputCWProxy {
  const _$IntelEventInputCWProxyImpl(this._value);

  final IntelEventInput _value;

  @override
  IntelEventInput type(IntelEventInputTypeEnum type) => call(type: type);

  @override
  IntelEventInput title(String title) => call(title: title);

  @override
  IntelEventInput severity(Severity? severity) => call(severity: severity);

  @override
  IntelEventInput origin(IntelEventInputOriginEnum? origin) =>
      call(origin: origin);

  @override
  IntelEventInput geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  IntelEventInput startTime(DateTime? startTime) => call(startTime: startTime);

  @override
  IntelEventInput endTime(DateTime? endTime) => call(endTime: endTime);

  @override
  IntelEventInput notes(String? notes) => call(notes: notes);

  @override
  IntelEventInput members(List<IntelEventMemberInput> members) =>
      call(members: members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventInput call({
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? startTime = const $CopyWithPlaceholder(),
    Object? endTime = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
  }) {
    return IntelEventInput(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IntelEventInputTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as IntelEventInputOriginEnum?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      startTime: startTime == const $CopyWithPlaceholder()
          ? _value.startTime
          // ignore: cast_nullable_to_non_nullable
          : startTime as DateTime?,
      endTime: endTime == const $CopyWithPlaceholder()
          ? _value.endTime
          // ignore: cast_nullable_to_non_nullable
          : endTime as DateTime?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<IntelEventMemberInput>,
    );
  }
}

extension $IntelEventInputCopyWith on IntelEventInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventInput.copyWith(...)` or `instanceOfIntelEventInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventInputCWProxy get copyWith => _$IntelEventInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventInput _$IntelEventInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelEventInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'title', 'members']);
      final val = IntelEventInput(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$IntelEventInputTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        origin: $checkedConvert(
          'origin',
          (v) => $enumDecodeNullable(_$IntelEventInputOriginEnumEnumMap, v),
        ),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        startTime: $checkedConvert(
          'startTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        endTime: $checkedConvert(
          'endTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        notes: $checkedConvert('notes', (v) => v as String?),
        members: $checkedConvert(
          'members',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    IntelEventMemberInput.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IntelEventInputToJson(IntelEventInput instance) =>
    <String, dynamic>{
      'type': _$IntelEventInputTypeEnumEnumMap[instance.type]!,
      'title': instance.title,
      'severity': ?_$SeverityEnumMap[instance.severity],
      'origin': ?_$IntelEventInputOriginEnumEnumMap[instance.origin],
      'geometry': ?instance.geometry?.toJson(),
      'startTime': ?instance.startTime?.toIso8601String(),
      'endTime': ?instance.endTime?.toIso8601String(),
      'notes': ?instance.notes,
      'members': instance.members.map((e) => e.toJson()).toList(),
    };

const _$IntelEventInputTypeEnumEnumMap = {
  IntelEventInputTypeEnum.maritimeDisruption: 'maritime-disruption',
  IntelEventInputTypeEnum.aviationAnomaly: 'aviation-anomaly',
  IntelEventInputTypeEnum.infrastructureDisruption: 'infrastructure-disruption',
  IntelEventInputTypeEnum.weatherImpact: 'weather-impact',
  IntelEventInputTypeEnum.seismic: 'seismic',
  IntelEventInputTypeEnum.custom: 'custom',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$IntelEventInputOriginEnumEnumMap = {
  IntelEventInputOriginEnum.agent: 'agent',
  IntelEventInputOriginEnum.operator_: 'operator',
};
