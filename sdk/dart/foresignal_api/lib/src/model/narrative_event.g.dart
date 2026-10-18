// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeEventCWProxy {
  NarrativeEvent event(NarrativeEventEventEnum event);

  NarrativeEvent narrative(NarrativeSummary narrative);

  NarrativeEvent changed(List<NarrativeEventChangedEnum>? changed);

  NarrativeEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeEvent call({
    NarrativeEventEventEnum event,
    NarrativeSummary narrative,
    List<NarrativeEventChangedEnum>? changed,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeEvent.copyWith(...)` or call `instanceOfNarrativeEvent.copyWith.fieldName(value)` for a single field.
class _$NarrativeEventCWProxyImpl implements _$NarrativeEventCWProxy {
  const _$NarrativeEventCWProxyImpl(this._value);

  final NarrativeEvent _value;

  @override
  NarrativeEvent event(NarrativeEventEventEnum event) => call(event: event);

  @override
  NarrativeEvent narrative(NarrativeSummary narrative) =>
      call(narrative: narrative);

  @override
  NarrativeEvent changed(List<NarrativeEventChangedEnum>? changed) =>
      call(changed: changed);

  @override
  NarrativeEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? narrative = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return NarrativeEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as NarrativeEventEventEnum,
      narrative: narrative == const $CopyWithPlaceholder() || narrative == null
          ? _value.narrative
          // ignore: cast_nullable_to_non_nullable
          : narrative as NarrativeSummary,
      changed: changed == const $CopyWithPlaceholder()
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as List<NarrativeEventChangedEnum>?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $NarrativeEventCopyWith on NarrativeEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeEvent.copyWith(...)` or `instanceOfNarrativeEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeEventCWProxy get copyWith => _$NarrativeEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeEvent _$NarrativeEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['event', 'narrative', 'at']);
      final val = NarrativeEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$NarrativeEventEventEnumEnumMap, v),
        ),
        narrative: $checkedConvert(
          'narrative',
          (v) => NarrativeSummary.fromJson(v as Map<String, dynamic>),
        ),
        changed: $checkedConvert(
          'changed',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$NarrativeEventChangedEnumEnumMap, e))
              .toList(),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeEventToJson(NarrativeEvent instance) =>
    <String, dynamic>{
      'event': _$NarrativeEventEventEnumEnumMap[instance.event]!,
      'narrative': instance.narrative.toJson(),
      'changed': ?instance.changed
          ?.map((e) => _$NarrativeEventChangedEnumEnumMap[e]!)
          .toList(),
      'at': instance.at.toIso8601String(),
    };

const _$NarrativeEventEventEnumEnumMap = {
  NarrativeEventEventEnum.narrativePeriodEmerged: 'narrative.emerged',
  NarrativeEventEventEnum.narrativePeriodUpdated: 'narrative.updated',
};

const _$NarrativeEventChangedEnumEnumMap = {
  NarrativeEventChangedEnum.state: 'state',
  NarrativeEventChangedEnum.status: 'status',
  NarrativeEventChangedEnum.postCount: 'postCount',
  NarrativeEventChangedEnum.evidenceGroupCount: 'evidenceGroupCount',
  NarrativeEventChangedEnum.velocity: 'velocity',
  NarrativeEventChangedEnum.merged: 'merged',
  NarrativeEventChangedEnum.split: 'split',
  NarrativeEventChangedEnum.title: 'title',
};
