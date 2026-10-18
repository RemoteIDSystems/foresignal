// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_selector_change_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventSelectorChangePointCWProxy {
  EventSelectorChangePoint family(String family);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorChangePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorChangePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  EventSelectorChangePoint call({String family});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventSelectorChangePoint.copyWith(...)` or call `instanceOfEventSelectorChangePoint.copyWith.fieldName(value)` for a single field.
class _$EventSelectorChangePointCWProxyImpl
    implements _$EventSelectorChangePointCWProxy {
  const _$EventSelectorChangePointCWProxyImpl(this._value);

  final EventSelectorChangePoint _value;

  @override
  EventSelectorChangePoint family(String family) => call(family: family);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorChangePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorChangePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventSelectorChangePoint call({
    Object? family = const $CopyWithPlaceholder(),
  }) {
    return EventSelectorChangePoint(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
    );
  }
}

extension $EventSelectorChangePointCopyWith on EventSelectorChangePoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventSelectorChangePoint.copyWith(...)` or `instanceOfEventSelectorChangePoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventSelectorChangePointCWProxy get copyWith =>
      _$EventSelectorChangePointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventSelectorChangePoint _$EventSelectorChangePointFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventSelectorChangePoint', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['family']);
  final val = EventSelectorChangePoint(
    family: $checkedConvert('family', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$EventSelectorChangePointToJson(
  EventSelectorChangePoint instance,
) => <String, dynamic>{'family': instance.family};
