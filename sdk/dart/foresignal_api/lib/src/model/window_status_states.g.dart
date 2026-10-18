// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_status_states.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowStatusStatesCWProxy {
  WindowStatusStates active(int? active);

  WindowStatusStates clear(int? clear);

  WindowStatusStates unknown(int? unknown);

  WindowStatusStates learning(int? learning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatusStates(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatusStates(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowStatusStates call({
    int? active,
    int? clear,
    int? unknown,
    int? learning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowStatusStates.copyWith(...)` or call `instanceOfWindowStatusStates.copyWith.fieldName(value)` for a single field.
class _$WindowStatusStatesCWProxyImpl implements _$WindowStatusStatesCWProxy {
  const _$WindowStatusStatesCWProxyImpl(this._value);

  final WindowStatusStates _value;

  @override
  WindowStatusStates active(int? active) => call(active: active);

  @override
  WindowStatusStates clear(int? clear) => call(clear: clear);

  @override
  WindowStatusStates unknown(int? unknown) => call(unknown: unknown);

  @override
  WindowStatusStates learning(int? learning) => call(learning: learning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatusStates(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatusStates(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowStatusStates call({
    Object? active = const $CopyWithPlaceholder(),
    Object? clear = const $CopyWithPlaceholder(),
    Object? unknown = const $CopyWithPlaceholder(),
    Object? learning = const $CopyWithPlaceholder(),
  }) {
    return WindowStatusStates(
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as int?,
      clear: clear == const $CopyWithPlaceholder()
          ? _value.clear
          // ignore: cast_nullable_to_non_nullable
          : clear as int?,
      unknown: unknown == const $CopyWithPlaceholder()
          ? _value.unknown
          // ignore: cast_nullable_to_non_nullable
          : unknown as int?,
      learning: learning == const $CopyWithPlaceholder()
          ? _value.learning
          // ignore: cast_nullable_to_non_nullable
          : learning as int?,
    );
  }
}

extension $WindowStatusStatesCopyWith on WindowStatusStates {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowStatusStates.copyWith(...)` or `instanceOfWindowStatusStates.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowStatusStatesCWProxy get copyWith =>
      _$WindowStatusStatesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowStatusStates _$WindowStatusStatesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WindowStatusStates', json, ($checkedConvert) {
      final val = WindowStatusStates(
        active: $checkedConvert('active', (v) => (v as num?)?.toInt()),
        clear: $checkedConvert('clear', (v) => (v as num?)?.toInt()),
        unknown: $checkedConvert('unknown', (v) => (v as num?)?.toInt()),
        learning: $checkedConvert('learning', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$WindowStatusStatesToJson(WindowStatusStates instance) =>
    <String, dynamic>{
      'active': ?instance.active,
      'clear': ?instance.clear,
      'unknown': ?instance.unknown,
      'learning': ?instance.learning,
    };
