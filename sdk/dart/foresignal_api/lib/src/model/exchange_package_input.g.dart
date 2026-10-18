// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exchange_package_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExchangePackageInputCWProxy {
  ExchangePackageInput objectRef(String objectRef);

  ExchangePackageInput recipients(Object? recipients);

  ExchangePackageInput message(String? message);

  ExchangePackageInput deliver(ExchangePackageInputDeliverEnum? deliver);

  ExchangePackageInput extract(ExchangePackageInputExtract? extract);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ExchangePackageInput call({
    String objectRef,
    Object? recipients,
    String? message,
    ExchangePackageInputDeliverEnum? deliver,
    ExchangePackageInputExtract? extract,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExchangePackageInput.copyWith(...)` or call `instanceOfExchangePackageInput.copyWith.fieldName(value)` for a single field.
class _$ExchangePackageInputCWProxyImpl
    implements _$ExchangePackageInputCWProxy {
  const _$ExchangePackageInputCWProxyImpl(this._value);

  final ExchangePackageInput _value;

  @override
  ExchangePackageInput objectRef(String objectRef) =>
      call(objectRef: objectRef);

  @override
  ExchangePackageInput recipients(Object? recipients) =>
      call(recipients: recipients);

  @override
  ExchangePackageInput message(String? message) => call(message: message);

  @override
  ExchangePackageInput deliver(ExchangePackageInputDeliverEnum? deliver) =>
      call(deliver: deliver);

  @override
  ExchangePackageInput extract(ExchangePackageInputExtract? extract) =>
      call(extract: extract);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExchangePackageInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExchangePackageInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExchangePackageInput call({
    Object? objectRef = const $CopyWithPlaceholder(),
    Object? recipients = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? deliver = const $CopyWithPlaceholder(),
    Object? extract = const $CopyWithPlaceholder(),
  }) {
    return ExchangePackageInput(
      objectRef: objectRef == const $CopyWithPlaceholder() || objectRef == null
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String,
      recipients: recipients == const $CopyWithPlaceholder()
          ? _value.recipients
          // ignore: cast_nullable_to_non_nullable
          : recipients as Object?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      deliver: deliver == const $CopyWithPlaceholder()
          ? _value.deliver
          // ignore: cast_nullable_to_non_nullable
          : deliver as ExchangePackageInputDeliverEnum?,
      extract: extract == const $CopyWithPlaceholder()
          ? _value.extract
          // ignore: cast_nullable_to_non_nullable
          : extract as ExchangePackageInputExtract?,
    );
  }
}

extension $ExchangePackageInputCopyWith on ExchangePackageInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExchangePackageInput.copyWith(...)` or `instanceOfExchangePackageInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExchangePackageInputCWProxy get copyWith =>
      _$ExchangePackageInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExchangePackageInput _$ExchangePackageInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExchangePackageInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['objectRef', 'recipients']);
  final val = ExchangePackageInput(
    objectRef: $checkedConvert('objectRef', (v) => v as String),
    recipients: $checkedConvert('recipients', (v) => v),
    message: $checkedConvert('message', (v) => v as String?),
    deliver: $checkedConvert(
      'deliver',
      (v) => $enumDecodeNullable(_$ExchangePackageInputDeliverEnumEnumMap, v),
    ),
    extract: $checkedConvert(
      'extract',
      (v) => v == null
          ? null
          : ExchangePackageInputExtract.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExchangePackageInputToJson(
  ExchangePackageInput instance,
) => <String, dynamic>{
  'objectRef': instance.objectRef,
  'recipients': instance.recipients,
  'message': ?instance.message,
  'deliver': ?_$ExchangePackageInputDeliverEnumEnumMap[instance.deliver],
  'extract': ?instance.extract?.toJson(),
};

const _$ExchangePackageInputDeliverEnumEnumMap = {
  ExchangePackageInputDeliverEnum.peer: 'peer',
  ExchangePackageInputDeliverEnum.file: 'file',
};
