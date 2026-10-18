// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'finding_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FindingEventCWProxy {
  FindingEvent event(FindingEventEventEnum event);

  FindingEvent finding(FindingEventFinding finding);

  FindingEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  FindingEvent call({
    FindingEventEventEnum event,
    FindingEventFinding finding,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFindingEvent.copyWith(...)` or call `instanceOfFindingEvent.copyWith.fieldName(value)` for a single field.
class _$FindingEventCWProxyImpl implements _$FindingEventCWProxy {
  const _$FindingEventCWProxyImpl(this._value);

  final FindingEvent _value;

  @override
  FindingEvent event(FindingEventEventEnum event) => call(event: event);

  @override
  FindingEvent finding(FindingEventFinding finding) => call(finding: finding);

  @override
  FindingEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FindingEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? finding = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return FindingEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as FindingEventEventEnum,
      finding: finding == const $CopyWithPlaceholder() || finding == null
          ? _value.finding
          // ignore: cast_nullable_to_non_nullable
          : finding as FindingEventFinding,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $FindingEventCopyWith on FindingEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFindingEvent.copyWith(...)` or `instanceOfFindingEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FindingEventCWProxy get copyWith => _$FindingEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FindingEvent _$FindingEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FindingEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['event', 'finding', 'at']);
      final val = FindingEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$FindingEventEventEnumEnumMap, v),
        ),
        finding: $checkedConvert(
          'finding',
          (v) => FindingEventFinding.fromJson(v as Map<String, dynamic>),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$FindingEventToJson(FindingEvent instance) =>
    <String, dynamic>{
      'event': _$FindingEventEventEnumEnumMap[instance.event]!,
      'finding': instance.finding.toJson(),
      'at': instance.at.toIso8601String(),
    };

const _$FindingEventEventEnumEnumMap = {
  FindingEventEventEnum.findingPeriodCreated: 'finding.created',
  FindingEventEventEnum.findingPeriodUpdated: 'finding.updated',
  FindingEventEventEnum.findingPeriodDismissed: 'finding.dismissed',
};
