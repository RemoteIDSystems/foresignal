//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_share_link_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportShareLinkInput {
  /// Returns a new [ReportShareLinkInput] instance.
  ReportShareLinkInput({

     this.expiresAt,
  });

  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportShareLinkInput &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory ReportShareLinkInput.fromJson(Map<String, dynamic> json) => _$ReportShareLinkInputFromJson(json);

  Map<String, dynamic> toJson() => _$ReportShareLinkInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

