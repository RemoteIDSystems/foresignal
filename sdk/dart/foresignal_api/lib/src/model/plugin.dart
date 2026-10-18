//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'plugin.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Plugin {
  /// Returns a new [Plugin] instance.
  Plugin({

    required  this.name,

    required  this.version,

    required  this.abi,

    required  this.kind,

    required  this.sha256,

     this.actualSha256,

    required  this.hashMatches,

    required  this.enabled,

    required  this.state,

     this.error,

     this.sourceIds,

     this.loadedAt,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



      /// Always 1, the only ABI this host runs; a manifest declaring another is in state `error`
  @JsonKey(
    
    name: r'abi',
    required: true,
    includeIfNull: false,
  )


  final PluginAbiEnum abi;



      /// Always `parser`, the only kind of ABI v1; a manifest declaring another is in state `error`
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PluginKindEnum kind;



      /// Declared in plugin.json; the file's own hash when the manifest is invalid (empty when neither is known)
  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



      /// The SHA-256 of plugin.wasm as read now; absent when the file is missing
  @JsonKey(
    
    name: r'actualSha256',
    required: false,
    includeIfNull: false,
  )


  final String? actualSha256;



      /// True when the declared and actual hashes are equal
  @JsonKey(
    
    name: r'hashMatches',
    required: true,
    includeIfNull: false,
  )


  final bool hashMatches;



      /// True when the admin enabled this plugin (an approval bound to a SHA-256 is stored)
  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// `error`: the manifest or module is invalid; `hash-mismatch`: the file does not hash to the declared SHA-256, or to the approved one; `enabled`: enabled, plugins on, hashes match and the module loads; `disabled`: otherwise
  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final PluginStateEnum state;



      /// Why the plugin will not load or run, or null
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// `wasm-feed` sources using this plugin
  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;



      /// When its current module was last loaded to run (since this process started), or null
  @JsonKey(
    
    name: r'loadedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? loadedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Plugin &&
      other.name == name &&
      other.version == version &&
      other.abi == abi &&
      other.kind == kind &&
      other.sha256 == sha256 &&
      other.actualSha256 == actualSha256 &&
      other.hashMatches == hashMatches &&
      other.enabled == enabled &&
      other.state == state &&
      other.error == error &&
      other.sourceIds == sourceIds &&
      other.loadedAt == loadedAt;

    @override
    int get hashCode =>
        name.hashCode +
        version.hashCode +
        abi.hashCode +
        kind.hashCode +
        sha256.hashCode +
        actualSha256.hashCode +
        hashMatches.hashCode +
        enabled.hashCode +
        state.hashCode +
        (error == null ? 0 : error.hashCode) +
        sourceIds.hashCode +
        (loadedAt == null ? 0 : loadedAt.hashCode);

  factory Plugin.fromJson(Map<String, dynamic> json) => _$PluginFromJson(json);

  Map<String, dynamic> toJson() => _$PluginToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Always 1, the only ABI this host runs; a manifest declaring another is in state `error`
enum PluginAbiEnum {
@JsonValue(1)
number1('1');

const PluginAbiEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Always `parser`, the only kind of ABI v1; a manifest declaring another is in state `error`
enum PluginKindEnum {
@JsonValue(r'parser')
parser(r'parser');

const PluginKindEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `error`: the manifest or module is invalid; `hash-mismatch`: the file does not hash to the declared SHA-256, or to the approved one; `enabled`: enabled, plugins on, hashes match and the module loads; `disabled`: otherwise
enum PluginStateEnum {
@JsonValue(r'disabled')
disabled(r'disabled'),
@JsonValue(r'enabled')
enabled(r'enabled'),
@JsonValue(r'hash-mismatch')
hashMismatch(r'hash-mismatch'),
@JsonValue(r'error')
error(r'error');

const PluginStateEnum(this.value);

final String value;

@override
String toString() => value;
}


