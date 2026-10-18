// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_token_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UpdateTokenRequestCWProxy {
  UpdateTokenRequest licencePolicy(
    List<UpdateTokenRequestLicencePolicyEnum>? licencePolicy,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UpdateTokenRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UpdateTokenRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  UpdateTokenRequest call({
    List<UpdateTokenRequestLicencePolicyEnum>? licencePolicy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUpdateTokenRequest.copyWith(...)` or call `instanceOfUpdateTokenRequest.copyWith.fieldName(value)` for a single field.
class _$UpdateTokenRequestCWProxyImpl implements _$UpdateTokenRequestCWProxy {
  const _$UpdateTokenRequestCWProxyImpl(this._value);

  final UpdateTokenRequest _value;

  @override
  UpdateTokenRequest licencePolicy(
    List<UpdateTokenRequestLicencePolicyEnum>? licencePolicy,
  ) => call(licencePolicy: licencePolicy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UpdateTokenRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UpdateTokenRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UpdateTokenRequest call({
    Object? licencePolicy = const $CopyWithPlaceholder(),
  }) {
    return UpdateTokenRequest(
      licencePolicy: licencePolicy == const $CopyWithPlaceholder()
          ? _value.licencePolicy
          // ignore: cast_nullable_to_non_nullable
          : licencePolicy as List<UpdateTokenRequestLicencePolicyEnum>?,
    );
  }
}

extension $UpdateTokenRequestCopyWith on UpdateTokenRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUpdateTokenRequest.copyWith(...)` or `instanceOfUpdateTokenRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UpdateTokenRequestCWProxy get copyWith =>
      _$UpdateTokenRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UpdateTokenRequest _$UpdateTokenRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UpdateTokenRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['licencePolicy']);
      final val = UpdateTokenRequest(
        licencePolicy: $checkedConvert(
          'licencePolicy',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => $enumDecode(
                  _$UpdateTokenRequestLicencePolicyEnumEnumMap,
                  e,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UpdateTokenRequestToJson(UpdateTokenRequest instance) =>
    <String, dynamic>{
      'licencePolicy': instance.licencePolicy
          ?.map((e) => _$UpdateTokenRequestLicencePolicyEnumEnumMap[e]!)
          .toList(),
    };

const _$UpdateTokenRequestLicencePolicyEnumEnumMap = {
  UpdateTokenRequestLicencePolicyEnum.noNc: 'no-nc',
  UpdateTokenRequestLicencePolicyEnum.noSa: 'no-sa',
  UpdateTokenRequestLicencePolicyEnum.redistributable: 'redistributable',
};
