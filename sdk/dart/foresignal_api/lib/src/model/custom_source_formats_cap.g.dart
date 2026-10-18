// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_cap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsCapCWProxy {
  CustomSourceFormatsCap acceptNonActual(bool? acceptNonActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsCap(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsCap call({bool? acceptNonActual});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsCap.copyWith(...)` or call `instanceOfCustomSourceFormatsCap.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsCapCWProxyImpl
    implements _$CustomSourceFormatsCapCWProxy {
  const _$CustomSourceFormatsCapCWProxyImpl(this._value);

  final CustomSourceFormatsCap _value;

  @override
  CustomSourceFormatsCap acceptNonActual(bool? acceptNonActual) =>
      call(acceptNonActual: acceptNonActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsCap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsCap call({
    Object? acceptNonActual = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsCap(
      acceptNonActual: acceptNonActual == const $CopyWithPlaceholder()
          ? _value.acceptNonActual
          // ignore: cast_nullable_to_non_nullable
          : acceptNonActual as bool?,
    );
  }
}

extension $CustomSourceFormatsCapCopyWith on CustomSourceFormatsCap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsCap.copyWith(...)` or `instanceOfCustomSourceFormatsCap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsCapCWProxy get copyWith =>
      _$CustomSourceFormatsCapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsCap _$CustomSourceFormatsCapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsCap', json, ($checkedConvert) {
  final val = CustomSourceFormatsCap(
    acceptNonActual: $checkedConvert(
      'acceptNonActual',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsCapToJson(
  CustomSourceFormatsCap instance,
) => <String, dynamic>{'acceptNonActual': ?instance.acceptNonActual};
