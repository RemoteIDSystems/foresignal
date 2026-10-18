// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_type_capabilities.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelTypeCapabilitiesCWProxy {
  ChannelTypeCapabilities editInPlace(bool? editInPlace);

  ChannelTypeCapabilities attachments(bool? attachments);

  ChannelTypeCapabilities fanOut(bool? fanOut);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTypeCapabilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTypeCapabilities(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelTypeCapabilities call({
    bool? editInPlace,
    bool? attachments,
    bool? fanOut,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelTypeCapabilities.copyWith(...)` or call `instanceOfChannelTypeCapabilities.copyWith.fieldName(value)` for a single field.
class _$ChannelTypeCapabilitiesCWProxyImpl
    implements _$ChannelTypeCapabilitiesCWProxy {
  const _$ChannelTypeCapabilitiesCWProxyImpl(this._value);

  final ChannelTypeCapabilities _value;

  @override
  ChannelTypeCapabilities editInPlace(bool? editInPlace) =>
      call(editInPlace: editInPlace);

  @override
  ChannelTypeCapabilities attachments(bool? attachments) =>
      call(attachments: attachments);

  @override
  ChannelTypeCapabilities fanOut(bool? fanOut) => call(fanOut: fanOut);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTypeCapabilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTypeCapabilities(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelTypeCapabilities call({
    Object? editInPlace = const $CopyWithPlaceholder(),
    Object? attachments = const $CopyWithPlaceholder(),
    Object? fanOut = const $CopyWithPlaceholder(),
  }) {
    return ChannelTypeCapabilities(
      editInPlace: editInPlace == const $CopyWithPlaceholder()
          ? _value.editInPlace
          // ignore: cast_nullable_to_non_nullable
          : editInPlace as bool?,
      attachments: attachments == const $CopyWithPlaceholder()
          ? _value.attachments
          // ignore: cast_nullable_to_non_nullable
          : attachments as bool?,
      fanOut: fanOut == const $CopyWithPlaceholder()
          ? _value.fanOut
          // ignore: cast_nullable_to_non_nullable
          : fanOut as bool?,
    );
  }
}

extension $ChannelTypeCapabilitiesCopyWith on ChannelTypeCapabilities {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelTypeCapabilities.copyWith(...)` or `instanceOfChannelTypeCapabilities.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelTypeCapabilitiesCWProxy get copyWith =>
      _$ChannelTypeCapabilitiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelTypeCapabilities _$ChannelTypeCapabilitiesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelTypeCapabilities', json, ($checkedConvert) {
  final val = ChannelTypeCapabilities(
    editInPlace: $checkedConvert('editInPlace', (v) => v as bool?),
    attachments: $checkedConvert('attachments', (v) => v as bool?),
    fanOut: $checkedConvert('fanOut', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ChannelTypeCapabilitiesToJson(
  ChannelTypeCapabilities instance,
) => <String, dynamic>{
  'editInPlace': ?instance.editInPlace,
  'attachments': ?instance.attachments,
  'fanOut': ?instance.fanOut,
};
