// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'smtp_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SmtpTestRequestCWProxy {
  SmtpTestRequest to(String to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SmtpTestRequest call({String to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSmtpTestRequest.copyWith(...)` or call `instanceOfSmtpTestRequest.copyWith.fieldName(value)` for a single field.
class _$SmtpTestRequestCWProxyImpl implements _$SmtpTestRequestCWProxy {
  const _$SmtpTestRequestCWProxyImpl(this._value);

  final SmtpTestRequest _value;

  @override
  SmtpTestRequest to(String to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SmtpTestRequest call({Object? to = const $CopyWithPlaceholder()}) {
    return SmtpTestRequest(
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
    );
  }
}

extension $SmtpTestRequestCopyWith on SmtpTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSmtpTestRequest.copyWith(...)` or `instanceOfSmtpTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SmtpTestRequestCWProxy get copyWith => _$SmtpTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SmtpTestRequest _$SmtpTestRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SmtpTestRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['to']);
      final val = SmtpTestRequest(
        to: $checkedConvert('to', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SmtpTestRequestToJson(SmtpTestRequest instance) =>
    <String, dynamic>{'to': instance.to};
