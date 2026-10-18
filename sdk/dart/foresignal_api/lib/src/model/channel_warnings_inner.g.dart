// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_warnings_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelWarningsInnerCWProxy {
  ChannelWarningsInner code(String code);

  ChannelWarningsInner message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelWarningsInner call({String code, String message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelWarningsInner.copyWith(...)` or call `instanceOfChannelWarningsInner.copyWith.fieldName(value)` for a single field.
class _$ChannelWarningsInnerCWProxyImpl
    implements _$ChannelWarningsInnerCWProxy {
  const _$ChannelWarningsInnerCWProxyImpl(this._value);

  final ChannelWarningsInner _value;

  @override
  ChannelWarningsInner code(String code) => call(code: code);

  @override
  ChannelWarningsInner message(String message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelWarningsInner call({
    Object? code = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return ChannelWarningsInner(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $ChannelWarningsInnerCopyWith on ChannelWarningsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelWarningsInner.copyWith(...)` or `instanceOfChannelWarningsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelWarningsInnerCWProxy get copyWith =>
      _$ChannelWarningsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelWarningsInner _$ChannelWarningsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelWarningsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code', 'message']);
  final val = ChannelWarningsInner(
    code: $checkedConvert('code', (v) => v as String),
    message: $checkedConvert('message', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ChannelWarningsInnerToJson(
  ChannelWarningsInner instance,
) => <String, dynamic>{'code': instance.code, 'message': instance.message};
