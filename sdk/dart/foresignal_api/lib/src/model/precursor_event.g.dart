// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorEventCWProxy {
  PrecursorEvent event(PrecursorEventEventEnum event);

  PrecursorEvent precursor(PrecursorEventPrecursor precursor);

  PrecursorEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorEvent call({
    PrecursorEventEventEnum event,
    PrecursorEventPrecursor precursor,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorEvent.copyWith(...)` or call `instanceOfPrecursorEvent.copyWith.fieldName(value)` for a single field.
class _$PrecursorEventCWProxyImpl implements _$PrecursorEventCWProxy {
  const _$PrecursorEventCWProxyImpl(this._value);

  final PrecursorEvent _value;

  @override
  PrecursorEvent event(PrecursorEventEventEnum event) => call(event: event);

  @override
  PrecursorEvent precursor(PrecursorEventPrecursor precursor) =>
      call(precursor: precursor);

  @override
  PrecursorEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? precursor = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return PrecursorEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as PrecursorEventEventEnum,
      precursor: precursor == const $CopyWithPlaceholder() || precursor == null
          ? _value.precursor
          // ignore: cast_nullable_to_non_nullable
          : precursor as PrecursorEventPrecursor,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $PrecursorEventCopyWith on PrecursorEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorEvent.copyWith(...)` or `instanceOfPrecursorEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorEventCWProxy get copyWith => _$PrecursorEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorEvent _$PrecursorEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['event', 'precursor', 'at']);
      final val = PrecursorEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$PrecursorEventEventEnumEnumMap, v),
        ),
        precursor: $checkedConvert(
          'precursor',
          (v) => PrecursorEventPrecursor.fromJson(v as Map<String, dynamic>),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorEventToJson(PrecursorEvent instance) =>
    <String, dynamic>{
      'event': _$PrecursorEventEventEnumEnumMap[instance.event]!,
      'precursor': instance.precursor.toJson(),
      'at': instance.at.toIso8601String(),
    };

const _$PrecursorEventEventEnumEnumMap = {
  PrecursorEventEventEnum.precursorPeriodValidated: 'precursor.validated',
  PrecursorEventEventEnum.precursorPeriodStale: 'precursor.stale',
  PrecursorEventEventEnum.precursorPeriodRetired: 'precursor.retired',
};
