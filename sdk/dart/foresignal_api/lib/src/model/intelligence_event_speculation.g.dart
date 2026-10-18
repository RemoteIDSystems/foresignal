// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intelligence_event_speculation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelligenceEventSpeculationCWProxy {
  IntelligenceEventSpeculation label(
    IntelligenceEventSpeculationLabelEnum label,
  );

  IntelligenceEventSpeculation alternatives(
    List<IntelEventAlternative> alternatives,
  );

  IntelligenceEventSpeculation narrative(String? narrative);

  IntelligenceEventSpeculation narrativeOrigin(
    IntelligenceEventSpeculationNarrativeOriginEnum? narrativeOrigin,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventSpeculation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventSpeculation(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelligenceEventSpeculation call({
    IntelligenceEventSpeculationLabelEnum label,
    List<IntelEventAlternative> alternatives,
    String? narrative,
    IntelligenceEventSpeculationNarrativeOriginEnum? narrativeOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelligenceEventSpeculation.copyWith(...)` or call `instanceOfIntelligenceEventSpeculation.copyWith.fieldName(value)` for a single field.
class _$IntelligenceEventSpeculationCWProxyImpl
    implements _$IntelligenceEventSpeculationCWProxy {
  const _$IntelligenceEventSpeculationCWProxyImpl(this._value);

  final IntelligenceEventSpeculation _value;

  @override
  IntelligenceEventSpeculation label(
    IntelligenceEventSpeculationLabelEnum label,
  ) => call(label: label);

  @override
  IntelligenceEventSpeculation alternatives(
    List<IntelEventAlternative> alternatives,
  ) => call(alternatives: alternatives);

  @override
  IntelligenceEventSpeculation narrative(String? narrative) =>
      call(narrative: narrative);

  @override
  IntelligenceEventSpeculation narrativeOrigin(
    IntelligenceEventSpeculationNarrativeOriginEnum? narrativeOrigin,
  ) => call(narrativeOrigin: narrativeOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventSpeculation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventSpeculation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelligenceEventSpeculation call({
    Object? label = const $CopyWithPlaceholder(),
    Object? alternatives = const $CopyWithPlaceholder(),
    Object? narrative = const $CopyWithPlaceholder(),
    Object? narrativeOrigin = const $CopyWithPlaceholder(),
  }) {
    return IntelligenceEventSpeculation(
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as IntelligenceEventSpeculationLabelEnum,
      alternatives:
          alternatives == const $CopyWithPlaceholder() || alternatives == null
          ? _value.alternatives
          // ignore: cast_nullable_to_non_nullable
          : alternatives as List<IntelEventAlternative>,
      narrative: narrative == const $CopyWithPlaceholder()
          ? _value.narrative
          // ignore: cast_nullable_to_non_nullable
          : narrative as String?,
      narrativeOrigin: narrativeOrigin == const $CopyWithPlaceholder()
          ? _value.narrativeOrigin
          // ignore: cast_nullable_to_non_nullable
          : narrativeOrigin as IntelligenceEventSpeculationNarrativeOriginEnum?,
    );
  }
}

extension $IntelligenceEventSpeculationCopyWith
    on IntelligenceEventSpeculation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelligenceEventSpeculation.copyWith(...)` or `instanceOfIntelligenceEventSpeculation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelligenceEventSpeculationCWProxy get copyWith =>
      _$IntelligenceEventSpeculationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelligenceEventSpeculation _$IntelligenceEventSpeculationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelligenceEventSpeculation', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['label', 'alternatives']);
  final val = IntelligenceEventSpeculation(
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$IntelligenceEventSpeculationLabelEnumEnumMap, v),
    ),
    alternatives: $checkedConvert(
      'alternatives',
      (v) => (v as List<dynamic>)
          .map((e) => IntelEventAlternative.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    narrative: $checkedConvert('narrative', (v) => v as String?),
    narrativeOrigin: $checkedConvert(
      'narrativeOrigin',
      (v) => $enumDecodeNullable(
        _$IntelligenceEventSpeculationNarrativeOriginEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelligenceEventSpeculationToJson(
  IntelligenceEventSpeculation instance,
) => <String, dynamic>{
  'label': _$IntelligenceEventSpeculationLabelEnumEnumMap[instance.label]!,
  'alternatives': instance.alternatives.map((e) => e.toJson()).toList(),
  'narrative': ?instance.narrative,
  'narrativeOrigin':
      ?_$IntelligenceEventSpeculationNarrativeOriginEnumEnumMap[instance
          .narrativeOrigin],
};

const _$IntelligenceEventSpeculationLabelEnumEnumMap = {
  IntelligenceEventSpeculationLabelEnum.speculation: 'speculation',
};

const _$IntelligenceEventSpeculationNarrativeOriginEnumEnumMap = {
  IntelligenceEventSpeculationNarrativeOriginEnum.llm: 'llm',
};
