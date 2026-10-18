//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_build.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraBuild {
  /// Returns a new [InfraBuild] instance.
  InfraBuild({

    required  this.id,

    required  this.status,

    required  this.startedAt,

     this.finishedAt,

    required  this.rulesVersion,

     this.inputs,

     this.nodes,

     this.edgesByMethod,

     this.edgesByLabel,

     this.warnings,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final InfraBuildStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'rulesVersion',
    required: true,
    includeIfNull: false,
  )


  final String rulesVersion;



      /// sourceId → last seq read
  @JsonKey(
    
    name: r'inputs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? inputs;



  @JsonKey(
    
    name: r'nodes',
    required: false,
    includeIfNull: false,
  )


  final int? nodes;



  @JsonKey(
    
    name: r'edgesByMethod',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? edgesByMethod;



  @JsonKey(
    
    name: r'edgesByLabel',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? edgesByLabel;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraBuild &&
      other.id == id &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.rulesVersion == rulesVersion &&
      other.inputs == inputs &&
      other.nodes == nodes &&
      other.edgesByMethod == edgesByMethod &&
      other.edgesByLabel == edgesByLabel &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        id.hashCode +
        status.hashCode +
        startedAt.hashCode +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        rulesVersion.hashCode +
        inputs.hashCode +
        nodes.hashCode +
        edgesByMethod.hashCode +
        edgesByLabel.hashCode +
        warnings.hashCode;

  factory InfraBuild.fromJson(Map<String, dynamic> json) => _$InfraBuildFromJson(json);

  Map<String, dynamic> toJson() => _$InfraBuildToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraBuildStatusEnum {
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed');

const InfraBuildStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


