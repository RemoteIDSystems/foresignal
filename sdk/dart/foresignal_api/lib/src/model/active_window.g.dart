// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'active_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ActiveWindowCWProxy {
  ActiveWindow timezone(String timezone);

  ActiveWindow windows(List<ActiveWindowWindowsInner> windows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActiveWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActiveWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  ActiveWindow call({String timezone, List<ActiveWindowWindowsInner> windows});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfActiveWindow.copyWith(...)` or call `instanceOfActiveWindow.copyWith.fieldName(value)` for a single field.
class _$ActiveWindowCWProxyImpl implements _$ActiveWindowCWProxy {
  const _$ActiveWindowCWProxyImpl(this._value);

  final ActiveWindow _value;

  @override
  ActiveWindow timezone(String timezone) => call(timezone: timezone);

  @override
  ActiveWindow windows(List<ActiveWindowWindowsInner> windows) =>
      call(windows: windows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActiveWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActiveWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ActiveWindow call({
    Object? timezone = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
  }) {
    return ActiveWindow(
      timezone: timezone == const $CopyWithPlaceholder() || timezone == null
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String,
      windows: windows == const $CopyWithPlaceholder() || windows == null
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as List<ActiveWindowWindowsInner>,
    );
  }
}

extension $ActiveWindowCopyWith on ActiveWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfActiveWindow.copyWith(...)` or `instanceOfActiveWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ActiveWindowCWProxy get copyWith => _$ActiveWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ActiveWindow _$ActiveWindowFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ActiveWindow', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['timezone', 'windows']);
      final val = ActiveWindow(
        timezone: $checkedConvert('timezone', (v) => v as String),
        windows: $checkedConvert(
          'windows',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ActiveWindowWindowsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ActiveWindowToJson(ActiveWindow instance) =>
    <String, dynamic>{
      'timezone': instance.timezone,
      'windows': instance.windows.map((e) => e.toJson()).toList(),
    };
