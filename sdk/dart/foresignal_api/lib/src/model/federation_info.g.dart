// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationInfoCWProxy {
  FederationInfo instanceId(String instanceId);

  FederationInfo instanceName(String? instanceName);

  FederationInfo version(String version);

  FederationInfo apiVersion(String apiVersion);

  FederationInfo jwksUri(String jwksUri);

  FederationInfo capabilities(
    List<FederationInfoCapabilitiesEnum> capabilities,
  );

  FederationInfo time(DateTime time);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationInfo call({
    String instanceId,
    String? instanceName,
    String version,
    String apiVersion,
    String jwksUri,
    List<FederationInfoCapabilitiesEnum> capabilities,
    DateTime time,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationInfo.copyWith(...)` or call `instanceOfFederationInfo.copyWith.fieldName(value)` for a single field.
class _$FederationInfoCWProxyImpl implements _$FederationInfoCWProxy {
  const _$FederationInfoCWProxyImpl(this._value);

  final FederationInfo _value;

  @override
  FederationInfo instanceId(String instanceId) => call(instanceId: instanceId);

  @override
  FederationInfo instanceName(String? instanceName) =>
      call(instanceName: instanceName);

  @override
  FederationInfo version(String version) => call(version: version);

  @override
  FederationInfo apiVersion(String apiVersion) => call(apiVersion: apiVersion);

  @override
  FederationInfo jwksUri(String jwksUri) => call(jwksUri: jwksUri);

  @override
  FederationInfo capabilities(
    List<FederationInfoCapabilitiesEnum> capabilities,
  ) => call(capabilities: capabilities);

  @override
  FederationInfo time(DateTime time) => call(time: time);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationInfo call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? instanceName = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? apiVersion = const $CopyWithPlaceholder(),
    Object? jwksUri = const $CopyWithPlaceholder(),
    Object? capabilities = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
  }) {
    return FederationInfo(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      instanceName: instanceName == const $CopyWithPlaceholder()
          ? _value.instanceName
          // ignore: cast_nullable_to_non_nullable
          : instanceName as String?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      apiVersion:
          apiVersion == const $CopyWithPlaceholder() || apiVersion == null
          ? _value.apiVersion
          // ignore: cast_nullable_to_non_nullable
          : apiVersion as String,
      jwksUri: jwksUri == const $CopyWithPlaceholder() || jwksUri == null
          ? _value.jwksUri
          // ignore: cast_nullable_to_non_nullable
          : jwksUri as String,
      capabilities:
          capabilities == const $CopyWithPlaceholder() || capabilities == null
          ? _value.capabilities
          // ignore: cast_nullable_to_non_nullable
          : capabilities as List<FederationInfoCapabilitiesEnum>,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
    );
  }
}

extension $FederationInfoCopyWith on FederationInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationInfo.copyWith(...)` or `instanceOfFederationInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationInfoCWProxy get copyWith => _$FederationInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationInfo _$FederationInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FederationInfo', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'instanceId',
          'version',
          'apiVersion',
          'jwksUri',
          'capabilities',
          'time',
        ],
      );
      final val = FederationInfo(
        instanceId: $checkedConvert('instanceId', (v) => v as String),
        instanceName: $checkedConvert('instanceName', (v) => v as String?),
        version: $checkedConvert('version', (v) => v as String),
        apiVersion: $checkedConvert('apiVersion', (v) => v as String),
        jwksUri: $checkedConvert('jwksUri', (v) => v as String),
        capabilities: $checkedConvert(
          'capabilities',
          (v) => (v as List<dynamic>)
              .map(
                (e) => $enumDecode(_$FederationInfoCapabilitiesEnumEnumMap, e),
              )
              .toList(),
        ),
        time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$FederationInfoToJson(FederationInfo instance) =>
    <String, dynamic>{
      'instanceId': instance.instanceId,
      'instanceName': ?instance.instanceName,
      'version': instance.version,
      'apiVersion': instance.apiVersion,
      'jwksUri': instance.jwksUri,
      'capabilities': instance.capabilities
          .map((e) => _$FederationInfoCapabilitiesEnumEnumMap[e]!)
          .toList(),
      'time': instance.time.toIso8601String(),
    };

const _$FederationInfoCapabilitiesEnumEnumMap = {
  FederationInfoCapabilitiesEnum.changes: 'changes',
  FederationInfoCapabilitiesEnum.stream: 'stream',
  FederationInfoCapabilitiesEnum.snapshot: 'snapshot',
  FederationInfoCapabilitiesEnum.ingest: 'ingest',
};
