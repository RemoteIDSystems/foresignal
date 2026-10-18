// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_request_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessRequestInputCWProxy {
  AccessRequestInput message(String? message);

  AccessRequestInput offerShare(String? offerShare);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessRequestInput call({String? message, String? offerShare});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessRequestInput.copyWith(...)` or call `instanceOfAccessRequestInput.copyWith.fieldName(value)` for a single field.
class _$AccessRequestInputCWProxyImpl implements _$AccessRequestInputCWProxy {
  const _$AccessRequestInputCWProxyImpl(this._value);

  final AccessRequestInput _value;

  @override
  AccessRequestInput message(String? message) => call(message: message);

  @override
  AccessRequestInput offerShare(String? offerShare) =>
      call(offerShare: offerShare);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessRequestInput call({
    Object? message = const $CopyWithPlaceholder(),
    Object? offerShare = const $CopyWithPlaceholder(),
  }) {
    return AccessRequestInput(
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      offerShare: offerShare == const $CopyWithPlaceholder()
          ? _value.offerShare
          // ignore: cast_nullable_to_non_nullable
          : offerShare as String?,
    );
  }
}

extension $AccessRequestInputCopyWith on AccessRequestInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessRequestInput.copyWith(...)` or `instanceOfAccessRequestInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessRequestInputCWProxy get copyWith =>
      _$AccessRequestInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessRequestInput _$AccessRequestInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccessRequestInput', json, ($checkedConvert) {
      final val = AccessRequestInput(
        message: $checkedConvert('message', (v) => v as String?),
        offerShare: $checkedConvert('offerShare', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AccessRequestInputToJson(AccessRequestInput instance) =>
    <String, dynamic>{
      'message': ?instance.message,
      'offerShare': ?instance.offerShare,
    };
