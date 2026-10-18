// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_selector_unexplained_move.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventSelectorUnexplainedMoveCWProxy {
  EventSelectorUnexplainedMove family(String family);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorUnexplainedMove(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorUnexplainedMove(...).copyWith(id: 12, name: "My name")
  /// ```
  EventSelectorUnexplainedMove call({String family});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventSelectorUnexplainedMove.copyWith(...)` or call `instanceOfEventSelectorUnexplainedMove.copyWith.fieldName(value)` for a single field.
class _$EventSelectorUnexplainedMoveCWProxyImpl
    implements _$EventSelectorUnexplainedMoveCWProxy {
  const _$EventSelectorUnexplainedMoveCWProxyImpl(this._value);

  final EventSelectorUnexplainedMove _value;

  @override
  EventSelectorUnexplainedMove family(String family) => call(family: family);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorUnexplainedMove(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorUnexplainedMove(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventSelectorUnexplainedMove call({
    Object? family = const $CopyWithPlaceholder(),
  }) {
    return EventSelectorUnexplainedMove(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
    );
  }
}

extension $EventSelectorUnexplainedMoveCopyWith
    on EventSelectorUnexplainedMove {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventSelectorUnexplainedMove.copyWith(...)` or `instanceOfEventSelectorUnexplainedMove.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventSelectorUnexplainedMoveCWProxy get copyWith =>
      _$EventSelectorUnexplainedMoveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventSelectorUnexplainedMove _$EventSelectorUnexplainedMoveFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventSelectorUnexplainedMove', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['family']);
  final val = EventSelectorUnexplainedMove(
    family: $checkedConvert('family', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$EventSelectorUnexplainedMoveToJson(
  EventSelectorUnexplainedMove instance,
) => <String, dynamic>{'family': instance.family};
